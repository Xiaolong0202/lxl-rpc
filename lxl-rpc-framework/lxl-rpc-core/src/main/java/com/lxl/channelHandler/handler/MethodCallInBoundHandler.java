package com.lxl.channelHandler.handler;

import com.lxl.LxlRpcBootStrap;
import com.lxl.ServiceConfig;
import com.lxl.transport.message.LxlRpcRequest;
import com.lxl.transport.message.RequestPayload;
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
        Object res = invokeMethod(payload,requestId);//根据请求体调用方法
        ctx.fireChannelRead(msg);
    }

    //使用反射去调用方法,服务的方法
    private Object invokeMethod(RequestPayload payload,long requestId) {
        ServiceConfig serviceConfig = LxlRpcBootStrap.SERVICE_CONFIG_CACHE.get(payload.getInterfaceName());
        Class interfaceClass = serviceConfig.getInterface();
        try {
            Method method = interfaceClass.getDeclaredMethod(payload.getMethodName(), payload.getMethodParametersClass());
            Object serviceImpl = serviceConfig.getRef();
            Object methodInvokeResult = method.invoke(serviceImpl, payload.getMethodParametersValue());
            System.out.println("methodInvokeResult = " + methodInvokeResult);
            return methodInvokeResult;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("请求【{}】使用反射进行方法调用实现时出现异常",requestId,e);
            throw new RuntimeException(e);
        }
    }
}
