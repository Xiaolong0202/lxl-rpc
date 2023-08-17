package com.lxl.enumnation;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  13:46
 **/
public enum ResponseType {
    SUCCESS((byte) 1,"请求成功"),FAILED((byte) 2,"请求失败");
    public final byte CODE;
    public final String description;
    ResponseType(byte CODE,String description){
        this.CODE = CODE;
        this.description = description;
    }
}
