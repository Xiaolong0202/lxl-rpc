package com.lxl.serialize.impl;

import com.lxl.exceptions.SerializerException;
import com.lxl.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  22:21
 **/
@Slf4j
public class JdkSerializerImpl implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null)return new byte[0];
        //TODO针对不同的消息做不同的处理,
        //进行对象的序列化与压缩
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("序列化【{}】的时候出现了错误",object);
            throw new SerializerException(e);
        }
    }

    @Override
    public <T> T disSerializer(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             ObjectInputStream objectInputStream = new ObjectInputStream(in)) {
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("反序列化失败", e);
            throw new SerializerException(e);
        }
    }

}
