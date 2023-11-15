package com.lxl;

import com.lxl.annotation.LxlRpcApi;
import com.lxl.channelHandler.ClientChannelInitializer;
import com.lxl.config.Configuration;
import com.lxl.core.HeartBeatDetector;
import com.lxl.core.ShutDownHolder;
import com.lxl.discovery.RegistryConfig;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.protection.circuit.CircuitBreaker;
import com.lxl.protection.rateLimite.RateLimiter;
import com.lxl.transport.message.request.LxlRpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

/**
 * 启动引导
 */
@Slf4j
public class LxlRpcBootStrap {


    //维护一个全局的配置
    private final Configuration configuration = new Configuration();

    //用于存放ip地址与channel之间的映射
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(256);

    //用于维护，接口名与方法实现类的映射
    public static final Map<String, ServiceConfig<?>> SERVICE_CONFIG_CACHE = new ConcurrentHashMap<>(256);

    //用于维护方法调用id与CompletableFuture的映射，我们需要使用CompletableFuture来获取方法调用的结果
    public static final Map<Long, CompletableFuture<Object>> COMPLETABLE_FUTURE_CACHE = new ConcurrentHashMap<>(256);


    //存储响应时间与ip地址的键值对，并且按照响应时间来排序
    public static final SortedMap<Long, InetSocketAddress> RESPONSE_TIME_CHANNEL_CACHE = Collections.synchronizedSortedMap(new TreeMap<>());


    //用于存放方法调用时候的请求,一次方法调用的线程对应者一次请求
    public static final ThreadLocal<LxlRpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    //ip地址也就是远程client端与他限流器映射，一个远程服务方要有一个限流器来进行限流

    public static final Map<SocketAddress, RateLimiter> IP_RATE_LIMITER = new ConcurrentHashMap<>(16);

    // ip地址与断路器的映射
    public static final Map<SocketAddress, CircuitBreaker> SERVICE_CIRCUIT_BREAKER = new ConcurrentHashMap<>(16);


    //是一个单例类
    private LxlRpcBootStrap() {
        //做一些初始化操作
    }

    private static final LxlRpcBootStrap instance = new LxlRpcBootStrap();

    public static LxlRpcBootStrap getInstance() {
        return instance;
    }

    /**
     * 用于定义当前应用的名字
     *
     * @param appName
     * @return
     */
    public LxlRpcBootStrap application(String appName) {
        this.configuration.setAppName(appName);
        return this;
    }

    /**
     * 用于配置注册中心,会根据url路径来返回相应的注册中心
     *
     * @param registryConfig
     * @return
     */
    public LxlRpcBootStrap registry(RegistryConfig registryConfig) {
        this.configuration.setRegistryConfig(registryConfig);
        return this;
    }


    /**
     * 配置负载均衡策略
     *
     * @param loadBalancer
     * @return
     */
    public LxlRpcBootStrap loadBalancer(LoadBalancer loadBalancer) {
        this.configuration.setLoadBalancer(loadBalancer);
        return this;
    }


    public LxlRpcBootStrap serialize(String serializeType) {
        this.configuration.setSerializeType(serializeType);
        return this;
    }

    /**
     * --------------------------------服务提供方的api------------------------------------------
     */


    /**
     * 发布服务,将接口服务发布到注册中心当中
     *
     * @param service
     * @return
     */
    public LxlRpcBootStrap publish(ServiceConfig<?> service) {
        //使用了抽象注册中心的概念,将服务注册到服务中心
        this.configuration.getRegistryConfig().getRegistry().register(service);
        //注册完成之后就开始发送心跳请求
        SERVICE_CONFIG_CACHE.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布服务
     *
     * @param services
     * @return
     */
    public LxlRpcBootStrap publish(List<ServiceConfig<?>> services) {
        services.forEach(this::publish);
        return this;
    }


    /**
     * 启动服务端的Netty服务,开启对应方法的监听
     */
    public void ServerStart() {
        //注册一个关闭应用程序的钩子
        Runtime.getRuntime().addShutdownHook(new LxlRpcShutDownThread());


        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ClientChannelInitializer());
        try {
            serverBootstrap.bind(this.configuration.getPORT()).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * --------------------------------服务调用方的api------------------------------------------
     */


    /**
     *
     * @param referenceConfig
     * @return
     */
    public void reference(ReferenceConfig<?> referenceConfig) {
        //在这个方法当中获取对应的配置项，用来配置reference,将来使用get方法的时候就可以获取代理对象
        referenceConfig.setRegistry(this.configuration.getRegistryConfig().getRegistry());
        HeartBeatDetector.detectorHeartBeat(referenceConfig.getInterface().getName());
    }

    public LxlRpcBootStrap compress(String compressType) {
        this.configuration.setCompressType(compressType);
        return this;
    }


    public LxlRpcBootStrap port(int port) {
        this.configuration.setPORT(port);
        return this;
    }

    /**
     * 扫描目标包内的所有被注解@LxlApi标注的类
     *
     * @param packageName
     * @return
     */
    public LxlRpcBootStrap scan(String packageName) {
        //通过包名获取其下的所有类的权限定名
        List<String> classesName = getAllClassesName(packageName);
        //通过反射获取它的接口，构建具体的实现
        List<? extends Class<?>> classList = classesName.stream()
                .map(cls -> {
                    try {
                        return Class.forName(cls);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(LxlRpcApi.class) != null)
                .toList();
        for (Class<?> clazz : classList) {
            Class<?>[] clazzInterfaces = clazz.getInterfaces();
            Object clazzInstance;
            //首先通过反射获取实例对象
            try {
                clazzInstance = clazz.getConstructor().newInstance();//使用空参的方法进行构造一个实例
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            for (Class<?> clazzInterface : clazzInterfaces) {
                ServiceConfig<Object> serviceConfig = new ServiceConfig<>();
                serviceConfig.setRef(clazzInstance);
                serviceConfig.setInterface(clazzInterface);
                this.publish(serviceConfig);
                if (log.isDebugEnabled()) {
                    log.debug("---->已经通过包扫描，将服务【{}】发布", serviceConfig.getInterface().getName());
                }
            }

        }
        //发布
        return this;
    }

    /**
     * 获取子包下面的所有的类名
     *
     * @param packageName
     * @return
     */
    private List<String> getAllClassesName(String packageName) {

        //通过包路径获取绝对路径
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null) {
            throw new RuntimeException("包扫描时出现异常");
        }
        String absolutePath = url.getPath();
        List<String> classesNames = new ArrayList<>();
        recursionFile(absolutePath, classesNames, basePath);
        System.out.println("classesNames.toString() = " + classesNames.toString());
        return classesNames;
    }

    /**
     * 递归调用获取所有的.class的类名
     *
     * @param absolutePath
     * @param classesNames
     * @param basePath
     */
    private void recursionFile(String absolutePath, List<String> classesNames, String basePath) {
        File file = new File(absolutePath);
        if (file.isDirectory()) {
            //如果是目录
            File[] files = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (files == null || files.length <= 0) return;
            for (File child : files) {
                recursionFile(child.getAbsolutePath(), classesNames, basePath);
            }
        } else if (file.isFile()) {
            //如果是文件
            classesNames.add(getClassNameByAbsolutePath(absolutePath, basePath));
        }
    }

    /**
     * 获取类名
     *
     * @param absolutePath
     * @param basePath
     * @return
     */
    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileName = absolutePath.substring(absolutePath.indexOf(basePath.replaceAll("/", Matcher.quoteReplacement("\\"))))
                .replaceAll("\\\\", ".");
        return fileName.substring(0, fileName.indexOf(".class"));
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private static class LxlRpcShutDownThread extends Thread {
        @Override
        public void run() {
            //打开挡板
            ShutDownHolder.baffle.set(true);
            long start = System.currentTimeMillis();
            //等待计数器归零,并且最多等十秒
            while (ShutDownHolder.requestCount.get() > 0) {
                if (System.currentTimeMillis() - start > 10000) break;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            //阻塞结束之后放行。可以执行其他操作，如释放内存
        }
    }


}
