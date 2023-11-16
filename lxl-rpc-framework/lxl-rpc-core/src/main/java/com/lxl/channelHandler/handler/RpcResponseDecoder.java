package com.lxl.channelHandler.handler;

import com.lxl.compress.Compressor;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.SerializeType;
import com.lxl.exceptions.NetWorkException;
import com.lxl.factory.CompressFactory;
import com.lxl.factory.SerializerFactory;
import com.lxl.serialize.Serializer;
import com.lxl.transport.message.response.LxlRpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 魔术值 6
 * 版本号 1
 * 头部长度 2
 * 总长度 8
 * 序列化类型 1
 * 压缩方式 1
 * 响应码  1
 * 请求的id 8
 * 请求体 未知
 * <p>
 * 客户端的接收服务端响应的解码器
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  14:41
 **/
@Slf4j
public class RpcResponseDecoder extends ByteToMessageDecoder {

//    public RpcResponseDecoder() {
//        super(1024 * 1024, MessageEncoderConstant.RESPONSE_LENGTH_FIELD_OFFSET, MessageEncoderConstant.LENGTH_FIELD_LENGTH);
//    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] magic = new byte[MessageEncoderConstant.MAGIC_NUM.length];
        in.readBytes(magic);
        //魔术值校验
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageEncoderConstant.MAGIC_NUM[i]) {
                throw new NetWorkException("consumer---魔术值校验异常,请求值异常");
            }
        }
        //解析版本
        byte version = in.readByte();
        if (version != MessageEncoderConstant.VERSION) throw new NetWorkException("provider---请求版本不支持");
        //解析头部长度
        short headLen = in.readShort();
        //解析总长度
        long fullLen = in.readLong();
        //序列化的类型
        byte serializeType = in.readByte();
        //压缩的类型
        byte compressType = in.readByte();
        //时间戳
        long timeStamp = in.readLong();
        //响应码
        byte responseCode = in.readByte();
        //请求的id
        long requestId = in.readLong();
        //请求体
        byte[] objectBytes = new byte[(int) (fullLen - headLen)];
        in.readBytes(objectBytes);
        System.out.println("objectBytes.length = " + objectBytes.length);
        Object bodyObject = null;
        if (objectBytes.length > 0) {
            //获取压缩器
            Compressor compressor = CompressFactory.getCompressorByCode(compressType).getImplement();
            //解压缩
            objectBytes = compressor.decompress(objectBytes);

            //反序列化
            Serializer serializer = SerializerFactory.getSerializerByCode(serializeType).getImplement();
            bodyObject = serializer.disSerializer(objectBytes, Object.class);
        }

        //封装响应
        LxlRpcResponse response = LxlRpcResponse.builder().object(bodyObject)
                .code(responseCode)
                .serializableType(serializeType)
                .compressType(compressType)
                .timeStamp(timeStamp)
                .requestId(requestId)
                .build();

        if (log.isDebugEnabled()) {
            log.debug("响应【{}】，已经在客户端完成解码,并封装成了对应的响应实体类", requestId);
        }
        ctx.fireChannelRead(response);
    }


}
