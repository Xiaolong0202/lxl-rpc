package com.lxl.enumnation;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  1:41
 **/
public enum RequestType {

    REQUEST((byte) 1,"普通请求"),HEART_BEAT((byte) 2,"心跳检测");
    public final byte ID;
    public final String description;
    RequestType(byte id,String description){
        this.ID = id;
        this.description = description;
    }

}
