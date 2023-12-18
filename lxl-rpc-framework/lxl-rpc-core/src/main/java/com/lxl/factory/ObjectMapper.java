package com.lxl.factory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/25  12:01
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectMapper<T> {

    private byte code;
    private String name;
    private T implement;
}
