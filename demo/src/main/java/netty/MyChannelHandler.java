package netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

public class MyChannelHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("服务端已经收到了消息:"+byteBuf.toString(Charset.forName("utf-8")));
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("你好，我是服务器，我已经收到消息了".getBytes(Charset.forName("utf-8"))));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("出错了");
        super.exceptionCaught(ctx, cause);
    }
}
