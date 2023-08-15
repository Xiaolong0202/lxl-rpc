package com.lxl.proxy.handler;

import com.lxl.LxlRpcBootStrap;
import com.lxl.NettyClientBootStrapInitializer;
import com.lxl.discovery.Registry;
import com.lxl.exceptions.NetWorkException;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectStreamClass;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


/**
 * 该类封装了客户端通信的基础
 * 1.发现服务 2.建立连接 3.发送请求 4.获取服务调用的结果
 */
@Slf4j
public class RpcInvocationHandler implements InvocationHandler {


    private Class interfaceRef;

    private Registry registry;

    public RpcInvocationHandler(Class interfaceRef, Registry registry) {
        this.interfaceRef = interfaceRef;
        this.registry = registry;
    }

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


        LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.put(1l,new CompletableFuture<>());
        CompletableFuture<Object> objectCompletableFuture = LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.get(1L);
        ChannelFuture channelFuture = channel.writeAndFlush(Unpooled.wrappedBuffer("我是客户端，".getBytes(StandardCharsets.UTF_8)));
        //添加监听器
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()){
                //需要捕获异步任务当中的异常
                objectCompletableFuture.completeExceptionally(future.cause());
            }
        });
        return objectCompletableFuture.get(3, TimeUnit.SECONDS);
    }
}
