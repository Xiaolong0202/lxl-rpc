package com.lxl.channelHandler;

import com.lxl.channelHandler.handler.MySimpleChannelInboundHandler;
import com.lxl.channelHandler.handler.RpcRequestToByteEncoder;
import com.lxl.channelHandler.handler.RpcResponseDecoder;
import com.lxl.channelHandler.handler.RpcResponseToByteEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG))//日志
                .addLast(new RpcRequestToByteEncoder())//发送请求的时候的编码器
                .addLast(new RpcResponseDecoder())//接受响应的解码器
                .addLast(new MySimpleChannelInboundHandler());
    }
}
