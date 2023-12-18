package com.lxl.serialize;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  22:12
 **/
public interface Serializer {

    /**
     * 将对象进行序列化的方法
     * @param object
     * @return
     */
    byte[] serialize(Object object);

    /**
     * 将字节数组进行反序列化的方法
     * @param bytes
     * @param clazz
     * @return
     * @param <T>
     */
    <T> T disSerializer(byte[] bytes,Class<T> clazz);

}
