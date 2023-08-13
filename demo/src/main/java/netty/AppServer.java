package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class AppServer {

    private int port;


    public AppServer(int port) {
        this.port = port;
    }

    public void start() throws Exception{
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);
        serverBootstrap.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                //处理入栈的请求
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new MyChannelHandler());
                    }
                });
        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        ChannelFuture channelFuture1 = channelFuture.channel().closeFuture();
    }

    public static void main(String[] args) throws Exception {
        new AppServer(8080).start();
    }
}
