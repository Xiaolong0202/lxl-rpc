package com.lxl.channelHandler.handler;

import com.lxl.enumnation.RequestType;
import com.lxl.exceptions.NetWorkException;
import com.lxl.transport.message.LxlRpcRequest;
import com.lxl.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc,服务端的解析报文的解码器
 * @DateTime 2023/8/16  19:51
 **/
@Slf4j
public class ProviderRequestDecoder extends LengthFieldBasedFrameDecoder {


    public ProviderRequestDecoder( ) {
        super(1024*1024, RequestMessageConstant.LENGTH_FIELD_OFFSET, RequestMessageConstant.LENGTH_FIELD_LENGTH);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            return decodeFrame(in);
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        //1.解析魔数值
        byte[] magic = new byte[RequestMessageConstant.MAGIC_NUM.length];
        byteBuf.readBytes(magic);
        //魔术值校验
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != RequestMessageConstant.MAGIC_NUM[i]){
                throw new NetWorkException("provider---魔术值校验异常,请求值异常");
            }
        }
        //解析版本
        byte version = byteBuf.readByte();
        if (version!=RequestMessageConstant.VERSION)throw new NetWorkException("provider---请求版本不支持");

        //解析头部长度
        short headLen = byteBuf.readShort();
        //解析总长度
        long fullLen = byteBuf.readLong();

        //序列化的类型
        byte serializeType = byteBuf.readByte();
        //压缩的类型
        byte compressType = byteBuf.readByte();
        //请求的类型 todo判断是否是心跳检测
        byte requestType = byteBuf.readByte();
        //请求的id
        long requestId = byteBuf.readLong();
        //请求体
        int payLoadLen = (int) (fullLen - headLen);
        byte [] requestBody = new byte[payLoadLen];
        byteBuf.readBytes(requestBody);
        //将请求体反序列化  心跳请求没有请求体
        RequestPayload requestPayload = null;
        if (requestType == RequestType.REQUEST.ID) {
             requestPayload =  getPayLoadObject(requestBody,requestId);
        }

        //得到请求
        LxlRpcRequest request = new LxlRpcRequest();
        request.setRequestId(requestId);
        request.setRequestType(requestType);
        request.setCompressType(compressType);
        request.setSerializableType(serializeType);
        request.setRequestPayload(requestPayload);

        return request;
    }

    private RequestPayload getPayLoadObject(byte[] requestBody,long requestId) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(requestBody);
                ObjectInputStream objectInputStream = new ObjectInputStream(in);){
            return (RequestPayload) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("请求【{}】,反序列化失败",requestId,e);
            throw new RuntimeException(e);
        }
    }
}
