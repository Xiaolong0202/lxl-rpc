package com.lxl.channelHandler.handler;

import com.lxl.compress.Compressor;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.RequestType;
import com.lxl.enumnation.SerializeType;
import com.lxl.exceptions.NetWorkException;
import com.lxl.factory.CompressFactory;
import com.lxl.factory.SerializerFactory;
import com.lxl.serialize.Serializer;
import com.lxl.transport.message.request.LxlRpcRequest;
import com.lxl.transport.message.request.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * lxl-rpc,服务端的解析报文的解码器
 * @Author LiuXiaolong
 * @Description lxl-rpc,服务端的解析报文的解码器
 * @DateTime 2023/8/16  19:51
 **/
@Slf4j
public class RpcRequestDecoder extends ByteToMessageDecoder {


//    public RpcRequestDecoder( ) {
//        //定义最长度，并且定位到该协议当中的长度字段，确定该帧的字节长度
////        super(1024*1024, MessageEncoderConstant.REQUEST_LENGTH_FIELD_OFFSET, MessageEncoderConstant.LENGTH_FIELD_LENGTH);
//        super(1024*1024, 1, 1);
//    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        LxlRpcRequest request =  decodeFrame(in);
        ctx.fireChannelRead(request);
    }

    private LxlRpcRequest decodeFrame(ByteBuf byteBuf) {
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
        //时间戳
        long timeStamp = byteBuf.readLong();

        log.debug("接受到请求，请求类型为【{}】",requestType);
        log.debug("获取的请求当中ByteBuf的长度为【{}】,通过解析报文得出的fullLength为【{}】",byteBuf.capacity(),fullLen);

        ///将请求体反序列化  心跳请求没有请求体
        RequestPayload requestPayload = null;
        if (requestType == RequestType.REQUEST.ID) {
            //请求体
            int payLoadLen = (int) (fullLen - headLen);
            byte[] requestBody = new byte[payLoadLen];
            byteBuf.readBytes(requestBody);

            //获取压缩器
            Compressor compressor = CompressFactory.getCompressorByCode(compressType).getImplement();
            //解压
            requestBody = compressor.decompress(requestBody);

            //获取序列化器
            Serializer serializer = SerializerFactory.getSerializerByCode(serializeType).getImplement();
            //反序列化
            requestPayload = serializer.disSerializer(requestBody, RequestPayload.class);
        }
        //得到请求
        LxlRpcRequest request = new LxlRpcRequest();
        request.setRequestId(requestId);
        request.setRequestType(requestType);
        request.setCompressType(compressType);
        request.setSerializableType(serializeType);
        request.setTimeStamp(timeStamp);
        request.setRequestPayload(requestPayload);

        if (log.isDebugEnabled()){
            log.debug("请求【{}】，在服务端已经完成解码,并封装成了对应的请求实体类--时间【{}】",requestId,new Date(timeStamp));
        }
        return request;
    }

}
