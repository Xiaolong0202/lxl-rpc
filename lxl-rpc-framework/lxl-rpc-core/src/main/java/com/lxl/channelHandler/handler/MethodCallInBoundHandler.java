package com.lxl.channelHandler.handler;

import com.lxl.LxlRpcBootStrap;
import com.lxl.ServiceConfig;
import com.lxl.enumnation.ResponseType;
import com.lxl.transport.message.request.LxlRpcRequest;
import com.lxl.transport.message.request.RequestPayload;
import com.lxl.transport.message.response.LxlRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  0:50
 **/
@Slf4j
public class MethodCallInBoundHandler extends SimpleChannelInboundHandler<LxlRpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LxlRpcRequest msg) throws Exception {
        RequestPayload payload = msg.getRequestPayload();
        long requestId = msg.getRequestId();
        Object res = invokeMethod(payload, requestId);//根据请求体调用方法

        //封装响应
        LxlRpcResponse response = LxlRpcResponse.builder()
                .compressType((byte) 1)
                .serializableType((byte) 1)
                .code(ResponseType.SUCCESS.CODE)
                .object(res)
                .requestId(msg.getRequestId())
                .build();
        if (log.isDebugEnabled()){
            log.debug("服务端响应封装完成");
        }
        //写出响应
        ctx.channel().writeAndFlush(response);
    }

    //使用反射去调用方法,服务的方法
    private Object invokeMethod(RequestPayload payload, long requestId) {
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
            if (log.isDebugEnabled()){
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


