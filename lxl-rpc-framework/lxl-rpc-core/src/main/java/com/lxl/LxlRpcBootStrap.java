package com.lxl;

import com.lxl.discovery.Registry;
import com.lxl.discovery.RegistryConfig;
import com.lxl.discovery.impl.ZookeeperRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;


public class LxlRpcBootStrap {

    Logger log = LoggerFactory.getLogger(LxlRpcBootStrap.class);


    public static Map<String,ServiceConfig> serviceAndImplMap = new ConcurrentHashMap<>(256);
    private String applicationName = "lxlRPC-default-application";
//    private RegistryConfig registryConfig;
    private ServiceConfig serviceConfig;
    private ProtocolConfig protocolConfig;

    private Registry registry;
    private int port = 8088;


    //是一个单例类

    private LxlRpcBootStrap(){
        //做一些初始化操作
    }

    private static LxlRpcBootStrap instance = new LxlRpcBootStrap();

    public static LxlRpcBootStrap getInstance() {
        return instance;
    }

    /**
     * 用于定义当前应用的名字
     * @param appName
     * @return
     */
    public LxlRpcBootStrap application(String appName) {
        this.applicationName = appName;
        return this;
    }

    /**
     * 用于配置注册中心,会根据url路径来返回相应的注册中心
     * @param registryConfig
     * @return
     */
    public LxlRpcBootStrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * 配置当前服务序列化的协议
     * @param protocalConfig
     * @return
     */
    public LxlRpcBootStrap protocol(ProtocolConfig protocalConfig) {
        this.protocolConfig = protocalConfig;
        if (log.isDebugEnabled()){
            log.debug("当前工程使用了:{}协议进行序列化",protocalConfig.toString());
        }
        return this;
    }


    /**
     * --------------------------------服务提供方的api------------------------------------------
     */


    /**
     * 发布服务,将接口服务发布到注册中心当中
     * @param service
     * @return
     */
    public LxlRpcBootStrap publish(ServiceConfig<?> service) {
        //使用了抽象注册中心的概念
        registry.register(service);
        serviceAndImplMap.put(service.getInterface().getName(),service);
        return this;
    }

    /**
     * 批量发布服务
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
    public void start(){
         while (true){

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
    public LxlRpcBootStrap reference(ReferenceConfig<?> referenceConfig) {
        //在这个方法当中获取对应的配置项，用来配置reference,将来使用get方法的时候就可以获取代理对象
        referenceConfig.setRegistry(registry);
        return this;
    }
}
