package com.lxl.factory;

import com.lxl.compress.Compressor;
import com.lxl.compress.impl.GzipCompressImpl;
import com.lxl.compress.impl.ZipCompressorImpl;
import com.lxl.enumnation.CompressType;
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
public class CompressFactory {
    //序列化器的缓存
    private static final Map<String, ObjectMapper<Compressor>> COMPRESS_NAME_CACHE = new ConcurrentHashMap<>(8);
    private static final Map<Byte, ObjectMapper<Compressor>> COMPRESS_CODE_CACHE = new ConcurrentHashMap<>(8);

    /**
     * 给工厂中新增压缩器
     * @param compressorObjectMapper
     */
    public static void addCompressor(ObjectMapper<Compressor> compressorObjectMapper){
        COMPRESS_NAME_CACHE.put(compressorObjectMapper.getName(),compressorObjectMapper);
        COMPRESS_CODE_CACHE.put(compressorObjectMapper.getCode(),compressorObjectMapper);
    }

    /**
     * 通过code获取Compressor
     * @param code
     * @return
     */
    public static ObjectMapper<Compressor> getCompressorByCode(Byte code) {
        ObjectMapper<Compressor> res = COMPRESS_CODE_CACHE.get(code);
        if (res == null){
            log.error("code:【{}】没有对应的压缩器",code);
        }
        return res;
    }

    /**
     * 通过名字获取Comressor
     * @param name
     * @return
     */
    public static ObjectMapper<Compressor> getCompressorByName(String name) {
        ObjectMapper<Compressor> res = COMPRESS_NAME_CACHE.get(name);
        if (res == null){
            log.error("name:【{}】没有对应的压缩器",name);
        }
        return res;
    }

    static{
        //先将自带的序列化方式放入工厂的缓存当中
        addCompressor(new ObjectMapper<Compressor>(CompressType.GZIP.ID,CompressType.GZIP.name(),new GzipCompressImpl()));
        addCompressor(new ObjectMapper<>(CompressType.ZIP.ID,CompressType.ZIP.name(),new ZipCompressorImpl()));
    }


}
