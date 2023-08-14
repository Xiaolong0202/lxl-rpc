package com.lxl;

import com.lxl.discovery.Registry;
import com.lxl.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;


@Slf4j
public class ReferenceConfig <T>{
    private Class<T> interfaceRef;

    private Registry registry;

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceProvider) {
        this.interfaceRef = interfaceProvider;
    }

    public T get() {
        //此处应当使用动态代理完成了部分工作,生成代理对象
        Object helloProxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{interfaceRef}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                System.out.println("proxy = " + proxy);
                System.out.println("method = " + method);
                System.out.println("args = " + args);
                System.out.println("hello proxy");
                //从注册中心找一个可用的服务
                InetSocketAddress inetSocketAddress = registry.lookup(interfaceRef.getName());
                if (log.isDebugEnabled()){
                    log.debug("服务调用方，返回了服务【{}】的可用主机【{}】",interfaceRef.getName(),inetSocketAddress.getHostString());
                }
                //使用netty连接服务器 发送服务的名字+方法的名字+参数列表,得到结果
                return null;
            }
        });
        return (T) helloProxy;
    }
}
