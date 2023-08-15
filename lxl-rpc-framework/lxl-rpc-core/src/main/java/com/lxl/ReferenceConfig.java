package com.lxl;

import com.lxl.discovery.Registry;
import com.lxl.exceptions.NetWorkException;
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

                //----------------------封装报文


                //使用netty连接服务器 发送服务的名字+方法的名字+参数列表,得到结果
                Channel channel = LxlRpcBootStrap.CHANNEL_CACHE.get(inetSocketAddress);
                if (channel==null) {
                    //连接服务器
                    CompletableFuture<Channel> channelCompletableFuture = new CompletableFuture();
                    //使用异步的方式获取
                    NettyClientBootStrapInitializer.getBootstrap().connect(inetSocketAddress).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()){
                                channelCompletableFuture.complete(future.channel());
                            }else {
                                channelCompletableFuture.completeExceptionally(future.cause());
                            }
                        }
                    });//用于阻塞直到返回结果
                    channel = channelCompletableFuture.get();
                    channel.writeAndFlush(Unpooled.copiedBuffer("你好吗，我是客户端".getBytes(StandardCharsets.UTF_8)));
                    //缓存
                    LxlRpcBootStrap.CHANNEL_CACHE.put(inetSocketAddress,channel);
                }
                if (channel==null)throw new NetWorkException("Netty获取channel对象实例失败");

                CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
                ChannelFuture channelFuture = channel.writeAndFlush(Unpooled.wrappedBuffer("我是客户端，".getBytes(StandardCharsets.UTF_8)));
                //添加监听器
                channelFuture.addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()){
                        //需要捕获异步任务当中的异常
                        objectCompletableFuture.completeExceptionally(future.cause());
                    }
                });

//                return objectCompletableFuture.get(3, TimeUnit.SECONDS);
                return null;
            }
        });
        return (T) helloProxy;
    }
}
