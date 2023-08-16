package com.lxl.proxy;

import com.lxl.channelHandler.handler.MySimpleChannelInboundHandler;
import com.lxl.channelHandler.handler.RpcRequestToByteEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new RpcRequestToByteEncoder())
                .addLast(new MySimpleChannelInboundHandler());
    }
}
