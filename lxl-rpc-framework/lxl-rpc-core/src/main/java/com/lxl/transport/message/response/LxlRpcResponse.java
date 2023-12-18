package com.lxl.transport.message.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  13:40
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LxlRpcResponse {
    private long requestId;
    private byte compressType;
    private byte serializableType;
    private long timeStamp;
    //响应码
    private byte code;
    //响应的内容
    private Object object;
}
