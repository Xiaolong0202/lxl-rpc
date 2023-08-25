package com.lxl.channelHandler.handler;

import com.lxl.compress.Compressor;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.RequestType;
import com.lxl.enumnation.SerializeType;
import com.lxl.factory.CompressFactory;
import com.lxl.factory.SerializerFactory;
import com.lxl.serialize.Serializer;
import com.lxl.transport.message.request.LxlRpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;


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
        byte[] payLoadBytes = new byte[0];
        if (msg.getRequestPayload() != null){
            //获取序列化器
            Serializer serializer = SerializerFactory.getSerializer(SerializeType.getSerializeType(msg.getSerializableType()));
            //序列化
            payLoadBytes = serializer.serialize(msg.getRequestPayload());
            //获取压缩器
            Compressor compressor = CompressFactory.getCompressorByCode(msg.getCompressType()).getImplement();
            //压缩
            payLoadBytes = compressor.compress(payLoadBytes);
        }
        long fullLength = MessageEncoderConstant.REQUEST_HEAD_LENGTH + payLoadBytes.length;
        //请求头
        out.writeBytes(MessageEncoderConstant.MAGIC_NUM);
        out.writeByte(MessageEncoderConstant.VERSION);
        out.writeShort(MessageEncoderConstant.REQUEST_HEAD_LENGTH);
        out.writeLong(fullLength);
        out.writeByte(msg.getSerializableType());
        out.writeByte(msg.getCompressType());
        out.writeByte(msg.getRequestType());
        out.writeLong(msg.getRequestId());
        out.writeLong(msg.getTimeStamp());
        if (msg.getRequestType() == RequestType.HEART_BEAT.ID)return;//如果是心跳请求，则可以直接返回，因为没有请求体
        //请求体
        out.writeBytes(payLoadBytes);
        if (log.isDebugEnabled()){
            log.debug("请求【{}】在客户端，已经完成编码",msg.getRequestId());
        }
    }

}
