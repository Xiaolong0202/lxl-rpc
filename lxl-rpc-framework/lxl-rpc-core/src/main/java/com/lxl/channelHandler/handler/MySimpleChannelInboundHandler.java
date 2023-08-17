package com.lxl.channelHandler.handler;

import com.lxl.LxlRpcBootStrap;
import com.lxl.transport.message.response.LxlRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<LxlRpcResponse>  {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LxlRpcResponse msg) throws Exception {
        String res = msg.toString();
        LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.get(1L).complete(res);
    }
}
