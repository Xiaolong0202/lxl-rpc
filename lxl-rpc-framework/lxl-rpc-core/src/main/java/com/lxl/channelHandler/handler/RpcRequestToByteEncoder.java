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
 * 报文的格式
 * 魔数 6
 * 版本 1
 * 首部长度 2
 * 报文总长度 8
 * 序列化类型 1
 * 压缩类型 1
 * 请求类型 1
 * 请求id 8
 * 时间戳 8
 * 请求体
 * <p>
 * 客户端的响应编码器
 * <p>
 * 请求体:未知长度
 * 出栈时候第一个处理器，先进行编码
 */

@Slf4j
public class RpcRequestToByteEncoder extends MessageToByteEncoder<LxlRpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext ctx, LxlRpcRequest msg, ByteBuf out) throws Exception {
        byte[] payLoadBytes = new byte[0];
        if (msg.getRequestPayload() != null) {
            //获取序列化器
            Serializer serializer = SerializerFactory.getSerializerByCode(msg.getSerializableType()).getImplement();
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
        if (msg.getRequestType() == RequestType.HEART_BEAT.ID) return;//如果是心跳请求，则可以直接返回，因为没有请求体
        //请求体
        out.writeBytes(payLoadBytes);
        if (log.isDebugEnabled()) {
            log.debug("请求【{}】在客户端，已经完成编码", msg.getRequestId());
        }

    }

}
