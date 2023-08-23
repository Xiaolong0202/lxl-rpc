package com.lxl.factory;

import com.lxl.compress.Compressor;
import com.lxl.compress.impl.GzipCompressImpl;
import com.lxl.compress.impl.ZipCompressorImpl;
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
    private static final Map<CompressType, Compressor> COMPRESS_CACHE = new ConcurrentHashMap<>(8);

    public static Compressor getSerializer(CompressType compressType) {
        Compressor compressor = COMPRESS_CACHE.get(compressType);
        if (compressor != null) return compressor;
        if (compressType == CompressType.ZIP) {
            compressor = new ZipCompressorImpl();
        } else if (compressType == CompressType.GZIP) {
            compressor = new GzipCompressImpl();
        } else {
            throw new SerializerException("给定的序列化类型没有对应的实现 ");
        }
        COMPRESS_CACHE.put(compressType, compressor);
        return compressor;
    }

}
