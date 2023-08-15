package com.lxl;

import com.lxl.channelHandler.handler.MySimpleChannelInboundHandler;
import com.lxl.proxy.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClientBootStrapInitializer {

    private static Bootstrap bootstrap = new Bootstrap();
    private static EventLoopGroup group = new NioEventLoopGroup();

    static {
        bootstrap.group(group)
                .channel(NioSocketChannel.class)//选择初始化什么channel
                .handler(new ConsumerChannelInitializer());
    }

    public static Bootstrap getBootstrap(){
        return bootstrap;
    }
}
