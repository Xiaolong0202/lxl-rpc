package com.lxl.channelHandler.handler;

import com.lxl.enumnation.ResponseType;
import com.lxl.transport.message.response.LxlRpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 报文格式
 *
 * 魔术值 6
 * 版本号 1
 * 头部长度 2
 * 总长度 8
 * 序列化类型 1
 * 压缩方式 1
 * 响应码  1
 * 请求的id 8
 * 请求体 未知
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  14:02
 **/
@Slf4j
public class RpcResponseToByteEncoder extends MessageToByteEncoder<LxlRpcResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, LxlRpcResponse msg, ByteBuf out) throws Exception {
        
        byte[] responseBody = getResponseBodyBytes(msg.getObject());
        out.writeBytes(MessageEncoderConstant.MAGIC_NUM);
        out.writeByte(MessageEncoderConstant.VERSION);
        out.writeShort(MessageEncoderConstant.RESPONSE_HEAD_LENGTH);
        out.writeLong(MessageEncoderConstant.RESPONSE_HEAD_LENGTH+responseBody.length);
        out.writeByte(msg.getSerializableType());
        out.writeByte(msg.getCompressType());
        out.writeByte(msg.getCode());
        out.writeLong(msg.getRequestId());
        out.writeBytes(responseBody);

        if (log.isDebugEnabled()){
            log.debug("响应【{}】在服务端，已经完成编码",msg.getRequestId());
        }
    }

    private byte[] getResponseBodyBytes(Object msg) {
        if (msg == null)return new byte[0];
        //TODO针对不同的消息做不同的处理,
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
