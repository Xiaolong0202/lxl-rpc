package com.lxl.enumnation;

import com.lxl.exceptions.SerializerException;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/18  16:37
 **/
public enum CompressType {
    GZIP((byte) 1),ZIP((byte) 2);

    public byte ID;

    CompressType(byte ID) {
        this.ID = ID;
    }

    public static CompressType getCompressType(byte id){
        Optional<CompressType> type = Stream.of(CompressType.values()).filter(compressType -> compressType.ID == id)
                .findFirst();
        if (type.isEmpty()){
            throw new RuntimeException("查找枚举时没有给定正确范围内的枚举ID");
        }
        return type.get();
    }
}
