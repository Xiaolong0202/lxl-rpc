package com.lxl.exceptions;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  22:31
 **/
public class SerializerException extends RuntimeException{


    public SerializerException(Throwable cause) {
        super(cause);
    }

    public SerializerException() {
    }

    public SerializerException(String message) {
        super(message);
    }
}
