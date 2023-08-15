package com.lxl;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;

public class NettyClientBootStrapInitializer {

    private static Bootstrap bootstrap = new Bootstrap();
    private static EventLoopGroup group = new NioEventLoopGroup();

    static {
        bootstrap.group(group)
                .channel(NioSocketChannel.class)//选择初始化什么channel
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                String res = ((ByteBuf) msg).toString(StandardCharsets.UTF_8);
                                LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.get(1L).complete(res);
                            }
                        });
                    }
                });
    }

    public static Bootstrap getBootstrap(){
        return bootstrap;
    }
}
