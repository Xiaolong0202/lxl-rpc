package com.lxl;

import com.lxl.discovery.Registry;
import com.lxl.exceptions.NetWorkException;
import com.lxl.proxy.handler.RpcInvocationHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Slf4j
public class ReferenceConfig <T>{
    private Class<T> interfaceRef;

    //应用的分组
    private String group = "default";

    private Registry registry;


    public ReferenceConfig(Class<T> interfaceRef, String group) {
        this.interfaceRef = interfaceRef;
        this.group = group;
    }

    public Registry getRegistry() {
        return registry;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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
        Object helloProxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader()
                , new Class[]{interfaceRef}
                , new RpcInvocationHandler(registry,interfaceRef,group));
        return (T) helloProxy;
    }
}
