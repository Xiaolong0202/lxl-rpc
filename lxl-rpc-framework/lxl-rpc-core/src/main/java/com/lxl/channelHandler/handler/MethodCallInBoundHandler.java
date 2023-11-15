package com.lxl.channelHandler.handler;

import com.lxl.LxlRpcBootStrap;
import com.lxl.ServiceConfig;
import com.lxl.core.ShutDownHolder;
import com.lxl.enumnation.RequestType;
import com.lxl.enumnation.ResponseType;
import com.lxl.protection.rateLimite.RateLimiter;
import com.lxl.protection.rateLimite.impl.TokenBuketRateLimiter;
import com.lxl.transport.message.request.LxlRpcRequest;
import com.lxl.transport.message.request.RequestPayload;
import com.lxl.transport.message.response.LxlRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;

/**
 * 方法调用的handler
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  0:50
 **/
@Slf4j
public class MethodCallInBoundHandler extends SimpleChannelInboundHandler<LxlRpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LxlRpcRequest msg) throws Exception {
        LxlRpcResponse response = null;
        //请求进来计数器先自增
        ShutDownHolder.requestCount.incrementAndGet();
        if (ShutDownHolder.baffle.get()){
            //如果客户端正在被关闭,直接写回
            response = LxlRpcResponse.builder()
                    .compressType(msg.getCompressType())
                    .serializableType(msg.getSerializableType())
                    .code(ResponseType.CLOSING.CODE)
                    .object(null)
                    .timeStamp(System.currentTimeMillis())
                    .requestId(msg.getRequestId())
                    .build();
            //写出响应
            ctx.channel().writeAndFlush(response);
            return;
        }
        //先试用限流器进行限流
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        RateLimiter rateLimiter =
                LxlRpcBootStrap.IP_RATE_LIMITER.get(socketAddress);
        if (rateLimiter == null) {
            rateLimiter = new TokenBuketRateLimiter(10, 10);
            LxlRpcBootStrap.IP_RATE_LIMITER.put(socketAddress, rateLimiter);
        }

        //具体的调用过程
        if (!rateLimiter.allowRequest()) {
            //被限流了
            response = LxlRpcResponse.builder()
                    .compressType(msg.getCompressType())
                    .serializableType(msg.getSerializableType())
                    .code(ResponseType.RATE_LIMIT.CODE)
                    .object(null)
                    .timeStamp(System.currentTimeMillis())
                    .requestId(msg.getRequestId())
                    .build();
        } else if (msg.getRequestType() == RequestType.HEART_BEAT.ID) {
            //处理心跳
            response = LxlRpcResponse.builder()
                    .compressType(msg.getCompressType())
                    .serializableType(msg.getSerializableType())
                    .code(ResponseType.HEART_BEAT_SUCCESS.CODE)
                    .object(null)
                    .timeStamp(System.currentTimeMillis())
                    .requestId(msg.getRequestId())
                    .build();

        } else {
            //正常的调用
            RequestPayload payload = msg.getRequestPayload();
            Object res = null;
            byte respCode = ResponseType.METHOD_CALL_SUCCESS.CODE;
            try {
                res = invokeMethod(payload);//根据请求体调用方法
            } catch (Exception e) {
                log.error("编号为【{}】的请求在方法调用过程当中发生异常", msg.getRequestId());
            }
            //封装响应
            response = LxlRpcResponse.builder()
                    .compressType(msg.getCompressType())
                    .serializableType(msg.getSerializableType())
                    .code(respCode)
                    .object(res)
                    .timeStamp(System.currentTimeMillis())
                    .requestId(msg.getRequestId())
                    .build();
            if (log.isDebugEnabled()) {
                log.debug("服务端响应封装完成");
                System.out.println("response = " + response);
            }
        }
        //写出响应
        ctx.channel().writeAndFlush(response);
    }

    //使用反射去调用方法,服务的方法
    private Object invokeMethod(RequestPayload payload) {
        if (payload == null) return new Object();
        try {
            //从缓存当中获取对应的serviceConfig
            ServiceConfig serviceConfig = LxlRpcBootStrap.SERVICE_CONFIG_CACHE.get(payload.getInterfaceName());
            Class interfaceClass = serviceConfig.getInterface();
            //使用反射直接去调用方法
            Method method = interfaceClass.getDeclaredMethod(payload.getMethodName(), payload.getMethodParametersClass());
            method.setAccessible(true);
            Object serviceImpl = serviceConfig.getRef();
            Object methodInvokeResult = method.invoke(serviceImpl, payload.getMethodParametersValue());
            System.out.println("methodInvokeResult = " + methodInvokeResult);
            if (log.isDebugEnabled()) {
                log.debug("服务端方法调用已经完成");
            }
            return methodInvokeResult;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("调用服务【{}】，使用反射进行【{}】方法调用实现时出现异常", payload.getInterfaceName(), payload.getMethodName(), e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {

    }
}


