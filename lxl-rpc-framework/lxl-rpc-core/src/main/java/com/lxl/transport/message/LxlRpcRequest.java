package com.lxl.transport.message;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LxlRpcRequest {

    //请求的类型
    private long requestId;

    private byte requestType;
    private byte compressType;
    private byte serializableType;

    //消息负载
    private RequestPayload requestPayload;
}
