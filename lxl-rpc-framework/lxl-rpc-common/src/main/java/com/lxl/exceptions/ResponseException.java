package com.lxl.exceptions;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/28  23:53
 **/
public class ResponseException extends RuntimeException{
    private String desc;
    private byte code;

    public ResponseException(byte code,String desc){
        super(desc);
        this.desc = desc;
        this.code = code;
    }
}
