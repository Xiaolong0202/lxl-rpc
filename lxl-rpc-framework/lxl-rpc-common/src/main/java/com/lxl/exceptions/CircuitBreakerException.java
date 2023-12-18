package com.lxl.exceptions;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  22:31
 **/
public class CircuitBreakerException extends RuntimeException{


    public CircuitBreakerException(Throwable cause) {
        super(cause);
    }

    public CircuitBreakerException() {
    }

    public CircuitBreakerException(String message) {
        super(message);
    }
}
