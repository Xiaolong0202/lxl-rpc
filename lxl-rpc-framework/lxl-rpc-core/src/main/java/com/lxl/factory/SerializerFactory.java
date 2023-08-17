package com.lxl.factory;

import com.lxl.enumnation.SerializeType;
import com.lxl.exceptions.SerializerException;
import com.lxl.serialize.Serializer;
import com.lxl.serialize.impl.JdkSerializerImpl;
import com.lxl.serialize.impl.JsonSerializerImpl;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  23:35
 **/
public class SerializerFactory {

    public static Serializer getSerializer(SerializeType serializeType){
        if (serializeType == SerializeType.JDK){
            return new JdkSerializerImpl();
        }else if (serializeType == SerializeType.JSON){
            return new JsonSerializerImpl();
        }else {
            throw new SerializerException("给定的序列化类型没有对应的实现 ");
        }
    }

}
