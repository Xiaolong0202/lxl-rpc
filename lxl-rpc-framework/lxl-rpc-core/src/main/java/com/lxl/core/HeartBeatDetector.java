package com.lxl.core;

import com.lxl.LxlRpcBootStrap;
import com.lxl.enumnation.RequestType;
import com.lxl.transport.message.request.LxlRpcRequest;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

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
    public static void detectorHeartBeat() {
        Thread thread = new Thread(() -> {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    LxlRpcBootStrap.CHANNEL_CACHE.forEach((inetSocketAddress, channel) -> {
                        long requestId = LxlRpcBootStrap.ID_GENERATOR.getId();
                        LxlRpcRequest rpcRequest = LxlRpcRequest.builder()
                                .requestId(requestId)
                                .compressType(LxlRpcBootStrap.compressType.ID)
                                .serializableType(LxlRpcBootStrap.serializeType.ID)
                                .requestType(RequestType.HEART_BEAT.ID)
                                .timeStamp(System.currentTimeMillis())
                                .build();
                        LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.put(requestId, new CompletableFuture<>());
                        long startTime = System.currentTimeMillis();
                        channel.writeAndFlush(rpcRequest).addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {
                                if (!future.isSuccess()) {
                                    LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.get(requestId).completeExceptionally(future.cause());
                                }
                            }
                        });
                        try {
                            LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.get(requestId).get(3, TimeUnit.SECONDS);
                            long endTime = System.currentTimeMillis();
                            long responeseTime = endTime - startTime;
                            if (log.isDebugEnabled()) {
                                log.debug("服务器【{}】的心跳响应时间为 {}", inetSocketAddress.toString(), responeseTime);
                            }
                            LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.put(responeseTime,channel);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }, 0, 5000);
        },"lxl-rpc-HeartBeatDetector-Thread");
        thread.setDaemon(true);//设置为守护线程
        thread.start();
    }

}
