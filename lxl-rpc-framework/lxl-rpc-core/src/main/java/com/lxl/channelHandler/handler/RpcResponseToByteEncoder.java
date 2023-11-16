package com.lxl.channelHandler.handler;

import com.lxl.compress.Compressor;
import com.lxl.core.ShutDownHolder;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.SerializeType;
import com.lxl.factory.CompressFactory;
import com.lxl.factory.SerializerFactory;
import com.lxl.serialize.Serializer;
import com.lxl.transport.message.response.LxlRpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 报文格式
 *
 * 魔数 6
 * 版本 1
 * 首部长度 2
 * 报文总长度 8
 * 序列化类型 1
 * 压缩类型 1
 * 响应码 1
 * 请求id 8
 * 时间戳 8
 *
 * 服务端的响应编码器
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  14:02
 **/
@Slf4j
public class RpcResponseToByteEncoder extends MessageToByteEncoder<LxlRpcResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, LxlRpcResponse msg, ByteBuf out) throws Exception {
        byte[] responseBody = new byte[0];
        if (msg.getObject() != null){
            Serializer serializer = SerializerFactory.getSerializerByCode(msg.getSerializableType()).getImplement();
            responseBody = serializer.serialize(msg.getObject());
            //获取压缩器
            Compressor compressor = CompressFactory.getCompressorByCode(msg.getCompressType()).getImplement();
            //压缩
            responseBody = compressor.compress(responseBody);
        }
        out.writeBytes(MessageEncoderConstant.MAGIC_NUM);
        out.writeByte(MessageEncoderConstant.VERSION);
        out.writeShort(MessageEncoderConstant.RESPONSE_HEAD_LENGTH);
        out.writeLong(MessageEncoderConstant.RESPONSE_HEAD_LENGTH+responseBody.length);
        out.writeByte(msg.getSerializableType());
        out.writeByte(msg.getCompressType());
        out.writeLong(msg.getTimeStamp());
        out.writeByte(msg.getCode());
        out.writeLong(msg.getRequestId());
        out.writeBytes(responseBody);
        //写出响应后应当在计数器减一
        ShutDownHolder.requestCount.decrementAndGet();
        if (log.isDebugEnabled()){
            log.debug("响应【{}】在服务端，已经完成编码",msg.getRequestId());
        }

    }


}
