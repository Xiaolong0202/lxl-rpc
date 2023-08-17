package com.lxl.enumnation;

import com.lxl.exceptions.SerializerException;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  22:57
 **/
public enum SerializeType {

    JDK((byte) 1),JSON((byte) 2);

    public byte ID;

    SerializeType(byte ID) {
        this.ID = ID;
    }

    public static SerializeType getSerializeType(byte id){
        Optional<SerializeType> serializeTypeOptional = Stream.of(SerializeType.values())
                .filter(serializeType -> serializeType.ID == id)
                .findFirst();
        if (serializeTypeOptional.isEmpty())throw new SerializerException("查找枚举时没有给定正确范围内的枚举ID");
        return serializeTypeOptional.get();
    }

    public static void main(String[] args) {
        SerializeType serializeType = SerializeType.getSerializeType((byte) 2);
        System.out.println(serializeType);
    }
}
