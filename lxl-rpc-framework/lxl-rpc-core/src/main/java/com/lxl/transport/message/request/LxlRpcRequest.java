package com.lxl.transport.message.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LxlRpcRequest implements Serializable {

    //请求的类型
    private long requestId;
    private byte requestType;
    private byte compressType;
    private byte serializableType;
    private long timeStamp;

    //消息负载
    private RequestPayload requestPayload;
}
