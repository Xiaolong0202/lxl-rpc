package com.lxl.serialize.impl;

import com.lxl.serialize.Serializer;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  23:37
 **/
public class JsonSerializerImpl implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        return new byte[0];
    }

    @Override
    public <T> T disSerializer(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
