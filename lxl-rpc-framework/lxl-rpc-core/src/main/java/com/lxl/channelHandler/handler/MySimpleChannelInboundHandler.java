package com.lxl.channelHandler.handler;

import com.lxl.LxlRpcBootStrap;
import com.lxl.enumnation.ResponseType;
import com.lxl.exceptions.ResponseException;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.protection.circuit.CircuitBreaker;
import com.lxl.transport.message.request.LxlRpcRequest;
import com.lxl.transport.message.response.LxlRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<LxlRpcResponse>  {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LxlRpcResponse msg) throws Exception {

        SocketAddress socketAddress = ctx.channel().remoteAddress();
        CircuitBreaker circuitBreaker = LxlRpcBootStrap.SERVICE_CIRCUIT_BREAKER.get(socketAddress);
        long requestId = msg.getRequestId();
        CompletableFuture<Object> objectCompletableFuture = LxlRpcBootStrap.COMPLETABLE_FUTURE_CACHE.get(requestId);
        byte code = msg.getCode();
        if (code == ResponseType.FAILED.CODE){
            circuitBreaker.countErrorRequest();
            objectCompletableFuture.complete(null);
            log.error("当前id为【{}】的请求，返回结果发生错误，响应码为【{}】",requestId,code);
            throw new ResponseException(code,ResponseType.FAILED.description);
        }else if (code == ResponseType.RATE_LIMIT.CODE){
            circuitBreaker.countErrorRequest();
            objectCompletableFuture.complete(null);
            log.error("当前id为【{}】的请求，返回结果发生错误，响应码为【{}】",requestId,code);
            throw new ResponseException(code,ResponseType.RATE_LIMIT.description);
        }else if (code == ResponseType.METHOD_NOT_FOUND.CODE){
            circuitBreaker.countErrorRequest();
            objectCompletableFuture.complete(null);
            log.error("当前id为【{}】的请求，返回结果发生错误，响应码为【{}】",requestId,code);
            throw new ResponseException(code,ResponseType.METHOD_NOT_FOUND.description);
        }else if (code == ResponseType.HEART_BEAT_SUCCESS.CODE){
            objectCompletableFuture.complete(null);
        }else if (code == ResponseType.CLOSING.CODE){
            log.debug("当前id为【{}】的请求，响应码为【{}】,访问被拒绝，原因是该服务器正在处于关闭状态",requestId,code);
            LxlRpcBootStrap.CHANNEL_CACHE.remove(socketAddress);
            LoadBalancer loadBalancer = LxlRpcBootStrap.getInstance().getConfiguration().getLoadBalancer();
            LxlRpcRequest lxlRpcRequest = LxlRpcBootStrap.REQUEST_THREAD_LOCAL.get();
            String serviceName = lxlRpcRequest.getRequestPayload().getInterfaceName();
            List<InetSocketAddress> inetSocketAddressList = LxlRpcBootStrap.getInstance().getConfiguration().getRegistryConfig().getRegistry().lookup(serviceName);
            loadBalancer.reLoadBalance(serviceName,inetSocketAddressList.stream().filter(inetSocketAddress -> inetSocketAddress.equals(socketAddress)).toList());
        } else {
            objectCompletableFuture.complete(msg.getObject());
        }
    }
}
