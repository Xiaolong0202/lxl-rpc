package com.lxl.exceptions;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  22:31
 **/
public class LoadBalanceException extends RuntimeException{


    public LoadBalanceException(Throwable cause) {
        super(cause);
    }

    public LoadBalanceException() {
    }

    public LoadBalanceException(String message) {
        super(message);
    }
}
