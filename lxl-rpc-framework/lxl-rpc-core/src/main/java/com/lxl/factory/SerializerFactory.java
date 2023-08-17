package com.lxl.factory;

import com.lxl.enumnation.SerializeType;
import com.lxl.exceptions.SerializerException;
import com.lxl.serialize.Serializer;
import com.lxl.serialize.impl.HessianSerializerImpl;
import com.lxl.serialize.impl.JdkSerializerImpl;
import com.lxl.serialize.impl.JsonSerializerImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化器的工厂
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  23:35
 **/
public class SerializerFactory {

    //序列化器的缓存
    private static final Map<SerializeType, Serializer> SERIALIZER_CACHE = new ConcurrentHashMap<>(8);

    public static Serializer getSerializer(SerializeType serializeType) {
        Serializer serializer = SERIALIZER_CACHE.get(serializeType);
        if (serializer != null) return serializer;
        if (serializeType == SerializeType.JDK) {
            serializer = new JdkSerializerImpl();
        } else if (serializeType == SerializeType.JSON) {
            serializer = new JsonSerializerImpl();
        } else if (serializeType == SerializeType.HESSIAN) {
            serializer = new HessianSerializerImpl();
        } else {
            throw new SerializerException("给定的序列化类型没有对应的实现 ");
        }
        SERIALIZER_CACHE.put(serializeType, serializer);
        return serializer;
    }

}
