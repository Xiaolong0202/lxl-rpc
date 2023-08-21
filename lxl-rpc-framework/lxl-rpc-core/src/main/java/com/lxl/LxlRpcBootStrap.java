package com.lxl;

import com.lxl.channelHandler.handler.MethodCallInBoundHandler;
import com.lxl.channelHandler.handler.RpcRequestDecoder;
import com.lxl.channelHandler.handler.RpcResponseToByteEncoder;
import com.lxl.core.HeartBeatDetector;
import com.lxl.discovery.Registry;
import com.lxl.discovery.RegistryConfig;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.SerializeType;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.loadbalance.impl.ConsistentLoadBalancer;
import com.lxl.loadbalance.impl.MinimumResponseTimeBalancer;
import com.lxl.loadbalance.impl.RoundLoadBalancer;
import com.lxl.transport.message.request.LxlRpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class LxlRpcBootStrap {


    Logger log = LoggerFactory.getLogger(LxlRpcBootStrap.class);

    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(256);
    public static final Map<String, ServiceConfig> SERVICE_CONFIG_CACHE = new ConcurrentHashMap<>(256);
    //用于存储completableFutrue,一个completableFutrue就维护这一次远程方法调用的操作
    public static final Map<Long, CompletableFuture<Object>> COMPLETABLE_FUTURE_CACHE = new ConcurrentHashMap<>(256);

    public static final SortedMap<Long,InetSocketAddress> RESPONSE_TIME_CHANNEL_CACHE = Collections.synchronizedSortedMap(new TreeMap<>());

    //用于存放方法调用时候的请求
    public static final ThreadLocal<LxlRpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    private String applicationName = "lxlRPC-default-application";
    //    private RegistryConfig registryConfig;
    private ServiceConfig serviceConfig;
    private ProtocolConfig protocolConfig;

    private Registry registry;

    public static LoadBalancer LOAD_BALANCER;

    public static final IdGenerator ID_GENERATOR = new IdGenerator(5, 5);//id生成器
    private int port = 8080;
    public static SerializeType serializeType;

    public static CompressType compressType;

    //是一个单例类

    private LxlRpcBootStrap() {
        //做一些初始化操作
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
        this.applicationName = appName;
        return this;
    }

    /**
     * 用于配置注册中心,会根据url路径来返回相应的注册中心
     *
     * @param registryConfig
     * @return
     */
    public LxlRpcBootStrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        LOAD_BALANCER = new MinimumResponseTimeBalancer();
        return this;
    }

    /**
     * 配置当前服务序列化的协议
     *
     * @param protocalConfig
     * @return
     */
    public LxlRpcBootStrap protocol(ProtocolConfig protocalConfig) {
        this.protocolConfig = protocalConfig;
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了:{}协议进行序列化", protocalConfig.toString());
        }
        return this;
    }

    public LxlRpcBootStrap serialize(SerializeType serializeType) {
        this.serializeType = serializeType;
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
        registry.register(service);
        SERVICE_CONFIG_CACHE.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布服务
     *
     * @param services
     * @return
     */
    public LxlRpcBootStrap publish(List<ServiceConfig> services) {
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
            serverBootstrap.bind(port).sync();
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
        referenceConfig.setRegistry(registry);
        HeartBeatDetector.detectorHeartBeat(referenceConfig.getInterface().getName());
        return this;
    }

    public LxlRpcBootStrap compress(CompressType compressType) {
        LxlRpcBootStrap.compressType = compressType;
        return this;
    }

    public int getPort() {
        return port;
    }

    public LxlRpcBootStrap port(int port) {
        this.port = port;
        return this;
    }

    public Registry getRegistry() {
        return registry;
    }
}
