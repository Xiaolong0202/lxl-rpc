package com.lxl.enumnation;

/**
 * 响应码做统一的处理
 * 成功码     20(方法调用成功)  21 (心跳成功返回)
 * 错误码     44(方法不存在)
 *           50 服务端出错
 * 负载码     31(服务器负载过高)
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  13:46
 **/
public enum ResponseType {
    METHOD_CALL_SUCCESS((byte) 20,"方法调用成功"),HEART_BEAT_SUCCESS((byte) 21,"心跳成功返回")
    ,FAILED((byte) 50,"服务端出错")
    ,METHOD_NOT_FOUND((byte) 44,"方法不存在")
    ,RATE_LIMIT((byte) 31,"服务器负载过高,限流");
    public final byte CODE;
    public final String description;
    ResponseType(byte CODE, String description){
        this.CODE = CODE;
        this.description = description;
    }
}
