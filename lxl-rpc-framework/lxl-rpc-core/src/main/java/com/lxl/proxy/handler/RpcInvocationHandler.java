package com.lxl.proxy.handler;

import com.lxl.LxlRpcBootStrap;
import com.lxl.NettyClientBootStrapInitializer;
import com.lxl.annotation.ReTry;
import com.lxl.discovery.Registry;
import com.lxl.enumnation.RequestType;
import com.lxl.exceptions.CircuitBreakerException;
import com.lxl.exceptions.NetWorkException;
import com.lxl.factory.CompressFactory;
import com.lxl.factory.SerializerFactory;
import com.lxl.protection.circuit.CircuitBreaker;
import com.lxl.transport.message.request.LxlRpcRequest;
import com.lxl.transport.message.request.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;
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


    /**
     * 所有的方法调用都会经过该节点,所以直接对这个方法中所有的代码进行try catch
     *
     * @param proxy  the proxy instance that the method was invoked on
     * @param method the {@code Method} instance corresponding to
     *               the interface method invoked on the proxy instance.  The declaring
     *               class of the {@code Method} object will be the interface that
     *               the method was declared in, which may be a superinterface of the
     *               proxy interface that the proxy class inherits the method through.
     * @param args   an array of objects containing the values of the
     *               arguments passed in the method invocation on the proxy instance,
     *               or {@code null} if interface method takes no arguments.
     *               Arguments of primitive types are wrapped in instances of the
     *               appropriate primitive wrapper class, such as
     *               {@code java.lang.Integer} or {@code java.lang.Boolean}.
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        ReTry reTryAnnotation = method.getAnnotation(ReTry.class);
        int tryTimes = 0;//定义异常重试的时间，默认值为0代表不重试
        int intervalTime = 0; //定义重试的间隔时间
        if (reTryAnnotation != null) {//如果有这个
            tryTimes = reTryAnnotation.tryTimes();
            intervalTime = reTryAnnotation.intervalTime();
        }
        CircuitBreaker circuitBreaker = null;

        while (true) {

            try {
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

                //---------获取断路器
                SocketAddress socketAddress = channel.remoteAddress();
               circuitBreaker  = LxlRpcBootStrap.SERVICE_CIRCUIT_BREAKER.get(socketAddress);
                if (circuitBreaker == null){
                    circuitBreaker = new CircuitBreaker(0.4, 10);
                    LxlRpcBootStrap.SERVICE_CIRCUIT_BREAKER.put(socketAddress, circuitBreaker);
                }

                if (circuitBreaker.isBreak()){
                    //如果是断路状态,则休眠一段时间再解除熔断状态
                    CircuitBreaker finalCircuitBreaker = circuitBreaker;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            finalCircuitBreaker.reset();
                        }
                    },5000);
                    throw new CircuitBreakerException("服务 "+interfaceRef.getName()+'.'+method.getName()+":"+socketAddress.toString()+" 熔断需要稍后再试");
                }


                LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.put(requestId, new CompletableFuture<>());
                CompletableFuture<Object> objectCompletableFuture = LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.get(requestId);
                //发送消息,请求
                ChannelFuture channelFuture = channel.writeAndFlush(rpcRequest);
                //熔断器记录请求
                circuitBreaker.countRequest();
                //添加监听器
                channelFuture.addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        //需要捕获异步任务当中的异常
                        objectCompletableFuture.completeExceptionally(future.cause());
                    }
                });

                //清理ThreadLocal
                LxlRpcBootStrap.REQUEST_THREAD_LOCAL.remove();
                //返回结果
                return objectCompletableFuture.get(3, TimeUnit.SECONDS);//如果返回时间超过三秒则视为相应失败

            } catch (Exception exception) {
                //熔断器记录失败的请求
                circuitBreaker.countErrorRequest();
                if ( exception instanceof CircuitBreakerException){
                    //如果是熔断异常则直接抛出
                    break;
                }
                tryTimes--;
                if (tryTimes < 0) {
                    log.error("对方法【{}】进行远程调用的时候，重试【{}】次，任然不可调用，放弃调用 ", method.getName(), reTryAnnotation.tryTimes() - tryTimes, exception);
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(intervalTime);
                log.error("在进行方法远程调用的时候发生了异常，即将进行重试", exception);
            }

        }
        throw new RuntimeException("执行远程调用方法 "+method.getName()+" 失败");
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
