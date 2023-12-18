package com.lxl.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.lxl.exceptions.SerializerException;
import com.lxl.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/18  0:53
 **/
@Slf4j
public class HessianSerializerImpl implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        try {
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            if (log.isDebugEnabled()) {
                log.debug("对象【{}】完成了序列化--HESSIAN", object);
            }
            return byteArray;
        } catch (IOException e) {
            log.error("序列化【{}】的时候出现了错误---HESSIAN", object);
            throw new SerializerException(e);
        }
    }

    @Override
    public <T> T disSerializer(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Hessian2Input hessian2Input = new Hessian2Input(byteArrayInputStream);
        try {
            T t = (T) hessian2Input.readObject();
            if (log.isDebugEnabled()) {
                log.debug("类【{}】完成了反序列化---HESSIAN", clazz.getName());
            }
            return t;
        } catch (IOException e) {
            log.error("类【{}】反序列化失败---HESSIAN", clazz.getName(), e);
            throw new SerializerException(e);
        }
    }
}
