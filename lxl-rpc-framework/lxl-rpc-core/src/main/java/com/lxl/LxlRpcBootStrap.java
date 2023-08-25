package com.lxl;

import com.lxl.annotation.LxlRpcApi;
import com.lxl.channelHandler.handler.MethodCallInBoundHandler;
import com.lxl.channelHandler.handler.RpcRequestDecoder;
import com.lxl.channelHandler.handler.RpcResponseToByteEncoder;
import com.lxl.config.Configuration;
import com.lxl.core.HeartBeatDetector;
import com.lxl.discovery.RegistryConfig;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.SerializeType;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.transport.message.request.LxlRpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
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
    //维护一个全局的配置中心
    private Configuration configuration;

    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(256);
    public static final Map<String, ServiceConfig> SERVICE_CONFIG_CACHE = new ConcurrentHashMap<>(256);
    //用于存储completableFutrue,一个completableFutrue就维护这一次远程方法调用的操作
    public static final Map<Long, CompletableFuture<Object>> COMPLETABLE_FUTURE_CACHE = new ConcurrentHashMap<>(256);
    //存储响应时间与ip地址的键值对，并且按照响应时间来排序
    public static final SortedMap<Long, InetSocketAddress> RESPONSE_TIME_CHANNEL_CACHE = Collections.synchronizedSortedMap(new TreeMap<>());

    //用于存放方法调用时候的请求
    public static final ThreadLocal<LxlRpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();


    //是一个单例类
    private LxlRpcBootStrap() {
        //做一些初始化操作
        this.configuration = new Configuration();
    }

    private static LxlRpcBootStrap instance = new LxlRpcBootStrap();

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
     * @param loadBalancer
     * @return
     */
    public LxlRpcBootStrap loadBalancer(LoadBalancer loadBalancer){
        this.configuration.setLoadBalancer(loadBalancer);
        return this;
    }


    /**
     * 配置当前服务序列化的协议
     *
     * @param protocalConfig
     * @return
     */
    public LxlRpcBootStrap protocol(ProtocolConfig protocalConfig) {
        this.configuration.setProtocolConfig(protocalConfig);
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了:{}协议进行序列化", protocalConfig.toString());
        }
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
        //使用了抽象注册中心的概念
        this.configuration.getRegistryConfig().getRegistry().register(service);
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
     * 启动Netty服务
     */
    public void start() {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LoggingHandler(LogLevel.DEBUG))
                                .addLast(new RpcRequestDecoder())//解码器
                                .addLast(new MethodCallInBoundHandler())//根据请求进行方法调用
                                .addLast(new RpcResponseToByteEncoder());//解析返回的相应
                    }
                });

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
     * @param referenceConfig
     * @return
     */
    public LxlRpcBootStrap reference(ReferenceConfig<?> referenceConfig) {
        //在这个方法当中获取对应的配置项，用来配置reference,将来使用get方法的时候就可以获取代理对象
        referenceConfig.setRegistry(this.configuration.getRegistryConfig().getRegistry());
        HeartBeatDetector.detectorHeartBeat(referenceConfig.getInterface().getName());
        return this;
    }

    public LxlRpcBootStrap compress(String compressType) {
        this.configuration.setCompressType(compressType);
        return this;
    }



    public LxlRpcBootStrap port(int port) {
        this.configuration.setPORT(port);
        return this;
    }

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
                }).filter(clazz -> {
                    return clazz.getAnnotation(LxlRpcApi.class) != null;
                })
                .toList();
        for (Class<?> clazz : classList) {
            Class<?>[] clazzInterfaces = clazz.getInterfaces();
            Object clazzInstance;
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
                if (log.isDebugEnabled()){
                    log.debug("---->已经通过包扫描，将服务【{}】发布",serviceConfig.getInterface().getName());
                }
            }

        }
        //发布
        return this;
    }

    private List<String> getAllClassesName(String packageName) {

        //通过包路径获取绝对路径
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null) {
            throw new RuntimeException("包扫描时出现异常");
        }
        String absolutePath = url.getPath();
        List<String> classesNames = new ArrayList<>();
        recursionFile(absolutePath, classesNames,basePath);
        System.out.println("classesNames.toString() = " + classesNames.toString());
        return classesNames;
    }

    private void recursionFile(String absolutePath, List<String> classesNames,String basePath) {
        File file = new File(absolutePath);
        if (file.isDirectory()) {
            //如果是目录
            File[] files = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (files == null || files.length <= 0) return;
            for (File child : files) {
                recursionFile(child.getAbsolutePath(), classesNames,basePath);
            }
        } else if (file.isFile()) {
            //如果是文件
            classesNames.add(getClassNameByAbsolutePath(absolutePath,basePath));
        }
    }

    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileName = absolutePath.substring(absolutePath.indexOf(basePath.replaceAll("/", Matcher.quoteReplacement("\\"))))
                .replaceAll("\\\\",".");
            return fileName.substring(0,fileName.indexOf(".class"));
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

}
