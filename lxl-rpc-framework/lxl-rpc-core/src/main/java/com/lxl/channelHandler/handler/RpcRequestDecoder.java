package com.lxl.channelHandler.handler;

import com.lxl.enumnation.RequestType;
import com.lxl.enumnation.SerializeType;
import com.lxl.exceptions.NetWorkException;
import com.lxl.factory.SerializerFactory;
import com.lxl.serialize.Serializer;
import com.lxl.transport.message.request.LxlRpcRequest;
import com.lxl.transport.message.request.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc,服务端的解析报文的解码器
 * @DateTime 2023/8/16  19:51
 **/
@Slf4j
public class RpcRequestDecoder extends LengthFieldBasedFrameDecoder {


    public RpcRequestDecoder( ) {
        super(1024*1024, MessageEncoderConstant.REQUEST_LENGTH_FIELD_OFFSET, MessageEncoderConstant.LENGTH_FIELD_LENGTH);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            return decodeFrame(in);
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        //1.解析魔数值
        byte[] magic = new byte[MessageEncoderConstant.MAGIC_NUM.length];
        byteBuf.readBytes(magic);
        //魔术值校验
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageEncoderConstant.MAGIC_NUM[i]){
                throw new NetWorkException("provider---魔术值校验异常,请求值异常");
            }
        }
        //解析版本
        byte version = byteBuf.readByte();
        if (version!= MessageEncoderConstant.VERSION)throw new NetWorkException("provider---请求版本不支持");

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
        //获取序列化器
        Serializer serializer = SerializerFactory.getSerializer(SerializeType.getSerializeType(serializeType));
        if (requestType == RequestType.REQUEST.ID) {
             requestPayload =  serializer.disSerializer(requestBody,RequestPayload.class);
        }

        //得到请求
        LxlRpcRequest request = new LxlRpcRequest();
        request.setRequestId(requestId);
        request.setRequestType(requestType);
        request.setCompressType(compressType);
        request.setSerializableType(serializeType);
        request.setRequestPayload(requestPayload);

        if (log.isDebugEnabled()){
            log.debug("请求【{}】，在服务端已经完成解码,并封装成了对应的请求实体类",requestId);
        }
        return request;
    }


}
