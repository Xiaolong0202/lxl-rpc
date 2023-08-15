package com.lxl;

import com.lxl.discovery.Registry;
import com.lxl.discovery.RegistryConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


@Slf4j
public class ReferenceConfig <T>{
    private Class<T> interfaceRef;

    private Registry registry;

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceProvider) {
        this.interfaceRef = interfaceProvider;
    }

    public T get() {
        //此处应当使用动态代理完成了部分工作,生成代理对象
        Object helloProxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{interfaceRef}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                System.out.println("proxy = " + proxy);
                System.out.println("method = " + method);
                System.out.println("args = " + args);
                System.out.println("hello proxy");
                //从注册中心找一个可用的服务
                InetSocketAddress inetSocketAddress = registry.lookup(interfaceRef.getName());
                if (log.isDebugEnabled()){
                    log.debug("服务调用方，返回了服务【{}】的可用主机【{}】",interfaceRef.getName(),inetSocketAddress.getHostString());
                }
                //使用netty连接服务器 发送服务的名字+方法的名字+参数列表,得到结果
//                NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
//                Bootstrap bootstrap = new Bootstrap();
//                bootstrap.group(nioEventLoopGroup)
//                        .channel(NioSocketChannel.class)
//                        .handler(new ChannelInitializer<SocketChannel>() {
//                            @Override
//                            protected void initChannel(SocketChannel ch) throws Exception {
//                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
//                                    @Override
//                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                        ByteBuf byteBuf = (ByteBuf) msg;
//                                        log.debug("consumer->获取到结果:{}",byteBuf.toString(Charset.forName("utf-8")));
//                                    }
//                                });
//                            }
//                        });
//                ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8088).sync();
//                channelFuture.channel().writeAndFlush(Unpooled.wrappedBuffer("你好吗，我是consumer".getBytes(Charset.forName("utf-8"))));
                NioEventLoopGroup group = new NioEventLoopGroup();
                //启动一个客户端需要一个辅助类
                Bootstrap bootstrap  = new Bootstrap();
                bootstrap.group(group)
                        .remoteAddress(new InetSocketAddress("127.0.0.1",8088))
                        .channel(NioSocketChannel.class)//选择初始化什么channel
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ByteBuf buf = (ByteBuf) msg;
                                        System.out.println(((ByteBuf) msg).toString(StandardCharsets.UTF_8));
                                    }
                                });
                            }
                        });
                ChannelFuture channelFuture = bootstrap.connect().sync();//Netty很多的方法是异步的，所以我们使用sync方法用于阻塞直到返回结果
                channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("你好吗，我是客户端".getBytes(StandardCharsets.UTF_8)));


                return null;
            }
        });
        return (T) helloProxy;
    }
}
