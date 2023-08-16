package com.lxl.channelHandler.handler;

import com.lxl.transport.message.LxlRpcRequest;
import com.lxl.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.*;


/**
 * 报文格式：
 * 魔术值:lxlRpc  6个字节  魔数
 * version版本  1
 * head length 首部的长度 2
 * full length 报文得到总长度 8
 * serialize  1
 * compress 1
 * requestType 1
 * requestId 8
 *
 * 请求体:未知长度
 * 出栈时候第一个处理器，先进行编码
 */

@Slf4j
public class RpcRequestToByteEncoder extends MessageToByteEncoder<LxlRpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, LxlRpcRequest msg, ByteBuf out) throws Exception {
        byte[] payLoadBytes = getPayLoadBytes(msg.getRequestPayload());
        long fullLength = RequestMessageConstant.HEAD_LENGTH + payLoadBytes.length;
        //请求头
        out.writeBytes(RequestMessageConstant.MAGIC_NUM);
        out.writeByte(RequestMessageConstant.VERSION);
        out.writeShort(RequestMessageConstant.HEAD_LENGTH);
        out.writeLong(fullLength);
        out.writeByte(msg.getSerializableType());
        out.writeByte(msg.getCompressType());
        out.writeByte(msg.getRequestType());
        out.writeLong(msg.getRequestId());
        //请求体
        out.writeBytes(payLoadBytes);
    }


    private byte[] getPayLoadBytes(RequestPayload msg){
        //进行对象的序列化与压缩
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(msg);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("序列化的时候出现了错误");
            throw new RuntimeException(e);
        }
    }


}
