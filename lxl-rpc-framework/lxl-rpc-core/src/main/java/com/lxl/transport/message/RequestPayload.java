package com.lxl.transport.message;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {

    private String interfaceName;
    private String methodName;
    //方法的参数类型
    private Class[] methodParametersClass;

    //被调用方法的形参值
    private Object[] methodParametersValue;

    //被调用方法的返回值
    private Class returnType;
}
