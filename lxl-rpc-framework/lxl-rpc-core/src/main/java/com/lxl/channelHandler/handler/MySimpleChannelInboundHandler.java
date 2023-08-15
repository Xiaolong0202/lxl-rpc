package com.lxl.channelHandler.handler;

import com.lxl.LxlRpcBootStrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.StandardCharsets;

public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf>  {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        String res = msg.toString(StandardCharsets.UTF_8);
        LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.get(1L).complete(res);
    }
}
