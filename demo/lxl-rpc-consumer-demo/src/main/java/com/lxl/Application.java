package com.lxl;

import com.lxl.core.HeartBeatDetector;
import com.lxl.discovery.RegistryConfig;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.SerializeType;

public class Application {
    public static void main(String[] args) {
        //想尽一切办法获取代理对象,使用对象对其进行封装
        ReferenceConfig<GreetingsService> referenceConfig = new ReferenceConfig<>(GreetingsService.class,"primary");

        LxlRpcBootStrap.getInstance()
//                .application("first-rpc-consumer")
//                .registry(new RegistryConfig("zookeeper://39.107.52.125:2181"))
//                .serialize(SerializeType.HESSIAN)
//                .compress(CompressType.GZIP)
                .reference(referenceConfig);

        //代理都  连接注册中心  获取服务列表  选择一个服务进行连接 发送请求（接口名，参数列表）

        GreetingsService greetingsService = referenceConfig.get();

        new Thread(()->{
            while (true){
                long s = System.currentTimeMillis();
                for (int i = 0; i < 10; i++) {
                    System.out.println("------------begin-------------------------------------------------");
                    String res = greetingsService.add(798,256);
                    System.out.println("res = " + res);
                    System.out.println("------------end------------------------------------------------");
                }
                System.out.println("System.currentTimeMillis() -s = " + (System.currentTimeMillis() - s));
            }
        }).start();


    }
}
