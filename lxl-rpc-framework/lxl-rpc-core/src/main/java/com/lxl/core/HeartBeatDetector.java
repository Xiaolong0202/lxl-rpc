package com.lxl.core;

import com.lxl.LxlRpcBootStrap;
import com.lxl.NettyClientBootStrapInitializer;
import com.lxl.config.Configuration;
import com.lxl.enumnation.RequestType;
import com.lxl.factory.CompressFactory;
import com.lxl.factory.SerializerFactory;
import com.lxl.transport.message.request.LxlRpcRequest;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/21  23:16
 **/
@Slf4j
public class HeartBeatDetector {
    //心跳的调度线程
    private volatile static Thread heartDetectorScheduledThread;
    //使用线程池当中的线程进行心跳处理，可以避免出现异常导致调度线程的停止
    private static final ThreadPoolExecutor HEART_BEAT_EXECUTOR = new ThreadPoolExecutor(
            200,
            500,
            2, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(200),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "heartBeatThread-" + count.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.DiscardOldestPolicy());//拒绝策略，丢掉阻塞队列中最前面的线程，因为心跳不是强制的

    /**
     * 拉取服务列表并进行连接,对没有连接的服务使用Netty创建连接,
     * 如果没有开启心跳检测的话则开始心跳检测
     */
    public static void detectorHeartBeat(String serviceName) {
        //从注册中心获取该服务的所有远程地址
        List<InetSocketAddress> inetSocketAddressList = LxlRpcBootStrap.getInstance().getConfiguration().getRegistryConfig().getRegistry().lookup(serviceName);
        //与所有没有建立连接的地址开始建立连接
        inetSocketAddressList.forEach(inetSocketAddress -> {
            if (!LxlRpcBootStrap.CHANNEL_CACHE.containsKey(inetSocketAddress)) {
                try {
                    ChannelFuture channelFuture = NettyClientBootStrapInitializer.getBootstrap().connect(inetSocketAddress).sync();
                    LxlRpcBootStrap.CHANNEL_CACHE.put(inetSocketAddress, channelFuture.channel());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        //借鉴懒汉式单例的实现模式，保证只有一个心跳调度线程
        if (heartDetectorScheduledThread == null) {
            synchronized (HeartBeatDetector.class) {
                if (heartDetectorScheduledThread == null) {
                    heartDetectorScheduledThread = new Thread(() -> new Timer().schedule(new HeartBeatTask(), 0, 3500), "lxl-rpc-HeartBeatDetector-Thread");
                    heartDetectorScheduledThread.setDaemon(true);//设置为守护线程
                    heartDetectorScheduledThread.start();
                }
            }
        }
    }

    /**
     * 实现心跳的调度
     */
    private static class HeartBeatTask extends TimerTask {
        @Override
        public void run() {
            //打印TreeMap
            log.info("---------------------------------TreeMap(由于有些响应时间相同，所以会覆盖掉部分inetSocketAddr)----------------------------------");
            LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.forEach((responese, inetSocket) -> log.info("responeseTime[{}],inetSocketAddress[{}]", responese, inetSocket));

            LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.clear();//每次先将响应时间的排序树清空
            //循环遍历所有的连接，并交给线程池去处理心跳请求
            LxlRpcBootStrap.CHANNEL_CACHE.forEach((inetSocketAddress, channel) -> HEART_BEAT_EXECUTOR.execute(()->{
                int tryTimes = 3;//请求异常重连
                int totalTimes = tryTimes;
                while (tryTimes-- > 0) {
                    Configuration configuration = LxlRpcBootStrap.getInstance().getConfiguration();
                    long requestId = configuration.getIdGenerator().getId();
                    LxlRpcRequest rpcRequest = LxlRpcRequest.builder()
                            .requestId(requestId)
                            .compressType(CompressFactory.getCompressorByName(configuration.getCompressType()).getCode())
                            .serializableType(SerializerFactory.getSerializerByName(configuration.getSerializeType()).getCode())
                            .requestType(RequestType.HEART_BEAT.ID)
                            .timeStamp(System.currentTimeMillis())
                            .build();
                    LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.put(requestId, new CompletableFuture<>());
                    long startTime = System.currentTimeMillis();//开始时间
                    //添加监听器
                    channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.get(requestId).completeExceptionally(future.cause());
                        }
                    });
                    try {
                        LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.get(requestId).get(3, TimeUnit.SECONDS);
                        long endTime = System.currentTimeMillis();
                        long responeseTime = endTime - startTime;
                        if (log.isDebugEnabled()) {
                            log.debug("服务器【{}】的心跳响应时间为 {}", inetSocketAddress.toString(), responeseTime);
                        }
                        LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.put(responeseTime, inetSocketAddress);
                        break;//如果成功就跳出循环
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        log.error("和地址为【{}】的主机连接发生异常，将尝试进行【{}】次重连", inetSocketAddress.toString(), totalTimes - tryTimes);
                        if (tryTimes == 0) {
                            //将失效的地址移出列表
                            LxlRpcBootStrap.CHANNEL_CACHE.remove(inetSocketAddress);
                            //todo 将还要将treeMap中的节点删除
                            log.error("将失效的地址【{}】移出列表", inetSocketAddress);
                        }
                        try {
                            Thread.sleep(10L * new Random().nextInt(1, 6));//睡一下，防止重试风暴问题
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }
                }
            }));
        }
    }
}
