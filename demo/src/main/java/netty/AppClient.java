package netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class AppClient {

    //Netty的客户端
    public void run() throws Exception{
        NioEventLoopGroup group = new NioEventLoopGroup();
        //启动一个客户端需要一个辅助类
        Bootstrap bootstrap  = new Bootstrap();
        bootstrap.group(group)
                .remoteAddress(new InetSocketAddress("127.0.0.1",8080))
                .channel(NioSocketChannel.class)//选择初始化什么channel
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                System.out.println(((ByteBuf) msg).toString(Charset.forName("utf-8")));
                            }
                        });
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect().sync();//Netty很多的方法是异步的，所以我们使用sync方法用于阻塞直到返回结果
        channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("你好吗，我是客户端".getBytes(Charset.forName("UTF-8"))));
    }

    public static void main(String[] args) throws Exception{
        new AppClient().run();
    }
}
