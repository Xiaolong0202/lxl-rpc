package com.lxl.proxy.handler;

import com.lxl.LxlRpcBootStrap;
import com.lxl.NettyClientBootStrapInitializer;
import com.lxl.discovery.Registry;
import com.lxl.enumnation.RequestType;
import com.lxl.exceptions.NetWorkException;
import com.lxl.factory.CompressFactory;
import com.lxl.factory.SerializerFactory;
import com.lxl.transport.message.request.LxlRpcRequest;
import com.lxl.transport.message.request.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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

        //----------------------封装报文
        //首先构建请求类
        RequestPayload payload = new RequestPayload(interfaceRef.getName(), method.getName(), method.getParameterTypes(), args, method.getReturnType());

        long requestId = LxlRpcBootStrap.getInstance().getConfiguration().getIdGenerator().getId();
        LxlRpcRequest rpcRequest = LxlRpcRequest.builder()
                .requestId(requestId)
                .compressType(CompressFactory.getCompressorByName(LxlRpcBootStrap.getInstance().getConfiguration().getCompressType()).getCode())
                .serializableType(SerializerFactory.getSerializerByName(LxlRpcBootStrap.getInstance().getConfiguration().getSerializeType()).getCode())
                .requestType(RequestType.REQUEST.ID)
                .timeStamp(System.currentTimeMillis())
                .requestPayload(payload)
                .build();
        //存储本地线程，在合适的时候remove
        LxlRpcBootStrap.REQUEST_THREAD_LOCAL.set(rpcRequest);

        //从注册中心找一个可用的服务

        //尝试使用负载均衡器来。选取一个可用的结点
        InetSocketAddress inetSocketAddress = LxlRpcBootStrap.getInstance().getConfiguration().getLoadBalancer().selectServiceAddr(interfaceRef.getName());
        System.out.println("inetSocketAddress = 选择的结点：：：：：：：：：：：：：：：：：：：：：：：" + inetSocketAddress);

        if (log.isDebugEnabled()) {
            log.debug("服务调用方，返回了服务【{}】的可用主机【{}】", interfaceRef.getName(), inetSocketAddress.getHostString());
        }


        //使用netty连接服务器 发送服务的名字+方法的名字+参数列表,得到结果
        Channel channel = this.getAvaliableChanel(inetSocketAddress);

        LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.put(requestId, new CompletableFuture<>());
        CompletableFuture<Object> objectCompletableFuture = LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.get(requestId);
        //发送消息,请求
        ChannelFuture channelFuture = channel.writeAndFlush(rpcRequest);
        //添加监听器
        channelFuture.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                //需要捕获异步任务当中的异常
                objectCompletableFuture.completeExceptionally(future.cause());
            }
        });

        //清理ThreadLocal



        return objectCompletableFuture.get(3, TimeUnit.SECONDS);//如果返回时间超过三秒则视为相应失败
    }

    /**
     * 获取可用的channel,先尝试从缓存当中获取，如果获取不到就使用Netty建立新的连接
     *
     * @param inetSocketAddress
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private Channel getAvaliableChanel(InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        Channel channel = LxlRpcBootStrap.CHANNEL_CACHE.get(inetSocketAddress);
        if (channel == null) {
            //连接服务器
            CompletableFuture<Channel> channelCompletableFuture = new CompletableFuture();
            //使用异步的方式获取
            NettyClientBootStrapInitializer.getBootstrap().connect(inetSocketAddress).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        channelCompletableFuture.complete(future.channel());
                    } else {
                        channelCompletableFuture.completeExceptionally(future.cause());
                    }
                }
            });//用于阻塞直到返回结果
            channel = channelCompletableFuture.get();
//            channel.writeAndFlush(Unpooled.copiedBuffer("你好吗，我是客户端".getBytes(StandardCharsets.UTF_8)));
            //缓存
            LxlRpcBootStrap.CHANNEL_CACHE.put(inetSocketAddress, channel);
        }
        if (channel == null) throw new NetWorkException("Netty获取channel对象实例失败");
        return channel;
    }
}
