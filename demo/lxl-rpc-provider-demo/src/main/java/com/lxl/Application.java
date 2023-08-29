package com.lxl;

import com.lxl.discovery.RegistryConfig;
import com.lxl.impl.GreetingsServiceImpl;

public class Application {
    public static void main(String[] args) {
        //服务提供方注册服务，启动服务
        //封装要发布的服务
        ServiceConfig<GreetingsService> service = new ServiceConfig();
        service.setInterface(GreetingsService.class);
        service.setRef(new GreetingsServiceImpl());

        //通过启动引导程序启动服务提供方
        //配置  应用的名称  注册中心  序列化协议  压缩方式
        //发布服务 启动服务
        LxlRpcBootStrap.getInstance()
//                .application("first-rpc-provider")
                .registry(new RegistryConfig("zookeeper://39.107.52.125:2181"))
//                .protocol(new ProtocolConfig("jdk"))
//                .port(3339)
//                .publish(service);
                .scan("com.lxl.impl")
                .start();
    }
}
