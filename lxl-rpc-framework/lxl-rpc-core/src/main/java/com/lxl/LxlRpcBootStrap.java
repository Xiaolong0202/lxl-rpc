package com.lxl;

import lombok.extern.slf4j.Slf4j;

import java.lang.module.ResolvedModule;
import java.util.List;


@Slf4j
public class LxlRpcBootStrap {
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
        return this;
    }

    /**
     * 用于配置注册中心
     * @param registryConfig
     * @return
     */
    public LxlRpcBootStrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 配置当前服务序列化的协议
     * @param protocalConfig
     * @return
     */
    public LxlRpcBootStrap protocol(ProtocolConfig protocalConfig) {
        if (log.isDebugEnabled()){
            log.debug("当前工程使用了:{}协议进行序列化",protocalConfig.toString());
        }
        return this;
    }


    /**
     * --------------------------------服务提供方的api------------------------------------------
     */


    /**
     * 发布服务,将接口实现并注册到服务中心
     * @param service
     * @return
     */
    public LxlRpcBootStrap publish(ServiceConfig<?> service) {
        if (log.isDebugEnabled()){
            log.debug("服务{},已经被注册",service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布服务
     * @param services
     * @return
     */
    public LxlRpcBootStrap publish(List<ServiceConfig> services) {

        return this;
    }


    /**
     * 启动Netty服务
     */
    public void start(){

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
        return this;
    }
}
