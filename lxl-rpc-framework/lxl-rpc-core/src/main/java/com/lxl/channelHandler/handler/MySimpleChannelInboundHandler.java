package com.lxl.channelHandler.handler;

import com.lxl.LxlRpcBootStrap;
import com.lxl.enumnation.ResponseType;
import com.lxl.exceptions.ResponseException;
import com.lxl.protection.circuit.CircuitBreaker;
import com.lxl.transport.message.response.LxlRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<LxlRpcResponse>  {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LxlRpcResponse msg) throws Exception {

        CircuitBreaker circuitBreaker = LxlRpcBootStrap.SERVICE_CIRCUIT_BREAKER.get(ctx.channel().remoteAddress());
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
        }else {
            objectCompletableFuture.complete(msg.getObject());
        }
    }
}
