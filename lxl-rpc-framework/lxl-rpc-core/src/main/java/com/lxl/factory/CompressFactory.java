package com.lxl.factory;

import com.lxl.compress.Compresser;
import com.lxl.compress.impl.GzipCompressImpl;
import com.lxl.compress.impl.ZipCompresserImpl;
import com.lxl.enumnation.CompressType;
import com.lxl.exceptions.SerializerException;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化器的工厂
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  23:35
 **/
public class CompressFactory {

    //序列化器的缓存
    private static final Map<CompressType, Compresser> COMPRESS_CACHE = new ConcurrentHashMap<>(8);

    public static Compresser getSerializer(CompressType compressType) {
        Compresser compresser = COMPRESS_CACHE.get(compressType);
        if (compresser != null) return compresser;
        if (compressType == CompressType.ZIP) {
            compresser = new ZipCompresserImpl();
        } else if (compressType == CompressType.GZIP) {
            compresser = new GzipCompressImpl();
        } else {
            throw new SerializerException("给定的序列化类型没有对应的实现 ");
        }
        COMPRESS_CACHE.put(compressType, compresser);
        return compresser;
    }

}
