package com.lxl.exceptions;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  22:31
 **/
public class CompressException extends RuntimeException{


    public CompressException(Throwable cause) {
        super(cause);
    }

    public CompressException() {
    }

    public CompressException(String message) {
        super(message);
    }
}
