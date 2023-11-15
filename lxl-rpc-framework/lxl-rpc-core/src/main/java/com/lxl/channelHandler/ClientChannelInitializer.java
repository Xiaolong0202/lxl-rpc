package com.lxl.channelHandler;

import com.lxl.channelHandler.handler.MethodCallInBoundHandler;
import com.lxl.channelHandler.handler.RpcRequestDecoder;
import com.lxl.channelHandler.handler.RpcResponseToByteEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/11/15  14:23
 **/
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new RpcRequestDecoder())//解码器
                .addLast(new MethodCallInBoundHandler())//根据请求进行方法调用
                .addLast(new RpcResponseToByteEncoder());//解析返回的相应
    }
}
