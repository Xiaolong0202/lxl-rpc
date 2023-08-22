package com.lxl.core;

import com.lxl.LxlRpcBootStrap;
import com.lxl.NettyClientBootStrapInitializer;
import com.lxl.enumnation.RequestType;
import com.lxl.transport.message.request.LxlRpcRequest;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/21  23:16
 **/
@Slf4j
public class HeartBeatDetector {

    /**
     * 拉取服务列表并进行连接
     */
    public static void detectorHeartBeat(String serviceName) {

        List<InetSocketAddress> inetSocketAddressList = LxlRpcBootStrap.getInstance().getRegistry().lookup(serviceName);
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

        Thread thread = new Thread(() -> new Timer().schedule(new HeartBeatTask(), 0, 5000), "lxl-rpc-HeartBeatDetector-Thread");
        thread.setDaemon(true);//设置为守护线程
        thread.start();
    }

    private static class HeartBeatTask extends TimerTask {
        @Override
        public void run() {
            LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.clear();//每次先将响应时间的排序树清空
            LxlRpcBootStrap.CHANNEL_CACHE.forEach((inetSocketAddress, channel) -> {
                int tryTimes = 3;//请求异常重连
                int totalTimes = tryTimes;
                while (tryTimes-- > 0) {
                    long requestId = LxlRpcBootStrap.ID_GENERATOR.getId();
                    LxlRpcRequest rpcRequest = LxlRpcRequest.builder()
                            .requestId(requestId)
                            .compressType(LxlRpcBootStrap.compressType.ID)
                            .serializableType(LxlRpcBootStrap.serializeType.ID)
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
            });

            //打印TreeMap
            log.info("---------------------------------TreeMap(由于有些响应时间相同，所以会覆盖掉部分inetSocketAddr)----------------------------------");
            LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.forEach((responese, inetSocket) -> log.info("responeseTime[{}],inetSocketAddress[{}]", responese, inetSocket));
        }
    }
}
