package com.lxl.factory;


import com.lxl.enumnation.SerializeType;
import com.lxl.exceptions.SerializerException;
import com.lxl.serialize.Serializer;
import com.lxl.serialize.impl.HessianSerializerImpl;
import com.lxl.serialize.impl.JdkSerializerImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化器的工厂
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  23:35
 **/
@Slf4j
public class SerializerFactory {
    private static final Map<String, ObjectMapper<Serializer>> SERIALIZER_NAME_CACHE = new ConcurrentHashMap<>(8);
    private static final Map<Byte, ObjectMapper<Serializer>> SERIALIZER_CODE_CACHE = new ConcurrentHashMap<>(8);

    /**
     * 给工厂中新增序列化器
     */
    public static void addSerializer(ObjectMapper<Serializer> compressorObjectMapper){
        SERIALIZER_NAME_CACHE.put(compressorObjectMapper.getName(),compressorObjectMapper);
        SERIALIZER_CODE_CACHE.put(compressorObjectMapper.getCode(),compressorObjectMapper);
    }

    /**
     * 通过code获取Serializer
     * @param code
     * @return
     */
    public static ObjectMapper<Serializer> getSerializerByCode(Byte code) {
        ObjectMapper<Serializer> res = SERIALIZER_CODE_CACHE.get(code);
        if (res == null){
            log.error("code:【{}】没有对应的压缩器",code);
        }
        return res;
    }

    /**
     * 通过名字获取序列化器
     * @param name
     * @return
     */
    public static ObjectMapper<Serializer> getSerializerByName(String name) {
        ObjectMapper<Serializer> res = SERIALIZER_NAME_CACHE.get(name);
        if (res == null){
            log.error("name:【{}】没有对应的序列化器",name);
            throw new SerializerException("没有对应的序列化器:"+name);
        }
        return res;
    }

    static{
        //先将自带的序列化方式放入工厂的缓存当中
        addSerializer(new ObjectMapper<>(SerializeType.HESSIAN.ID,SerializeType.HESSIAN.name(),new HessianSerializerImpl()));
        addSerializer(new ObjectMapper<>(SerializeType.JDK.ID,SerializeType.JDK.name(),new JdkSerializerImpl()));
    }

    public static void main(String[] args) {
        System.out.println();
    }
}
