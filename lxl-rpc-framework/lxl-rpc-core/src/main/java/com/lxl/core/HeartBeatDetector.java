package com.lxl.core;

import com.lxl.LxlRpcBootStrap;
import com.lxl.enumnation.RequestType;
import com.lxl.transport.message.request.LxlRpcRequest;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/21  23:16
 **/
public class HeartBeatDetector {

    /**
     * 拉取服务列表并进行连接
     */
    public static void detectorHeartBeat(String serviceName){
        List<InetSocketAddress> inetSocketAddressList = LxlRpcBootStrap.getInstance().getRegistry().lookup(serviceName);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LxlRpcBootStrap.CHANNEL_CACHE.forEach((inetSocketAddress,channel)->{
                    long requestId = LxlRpcBootStrap.ID_GENERATOR.getId();
                    LxlRpcRequest rpcRequest = LxlRpcRequest.builder()
                            .requestId(requestId)
                            .compressType(LxlRpcBootStrap.compressType.ID)
                            .serializableType(LxlRpcBootStrap.serializeType.ID)
                            .requestType(RequestType.HEART_BEAT.ID)
                            .timeStamp(System.currentTimeMillis())
                            .build();
                    try {
                        long startTime = System.currentTimeMillis();
                        boolean await = channel.writeAndFlush(rpcRequest).await(3, TimeUnit.SECONDS);
                        if (await){
                            long endTime = System.currentTimeMillis();
                            System.out.println("心跳检测正常");
                        }else {
                            throw new RuntimeException("连接结点"+inetSocketAddress+"超时");
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                });
            }
        },0,5000);
    }

}
