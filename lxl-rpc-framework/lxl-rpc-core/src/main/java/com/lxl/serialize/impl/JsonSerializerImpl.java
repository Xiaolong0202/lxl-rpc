package com.lxl.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.lxl.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;


/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  23:37
 **/
@Slf4j
public class JsonSerializerImpl implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        if (object == null) return new byte[0];
        byte[] jsonBytes = JSON.toJSONBytes(object);
        if (log.isDebugEnabled()) {
            log.debug("对象【{}】完成了序列化---JSON", object);
        }
        return jsonBytes;
    }

    @Override
    public <T> T disSerializer(byte[] bytes, Class<T> clazz) {
        T t = JSON.parseObject(bytes, clazz);
        if (log.isDebugEnabled()) {
            log.debug("类【{}】完成了反序列化---JSON", clazz.getName());
        }
        return t;
    }
}
