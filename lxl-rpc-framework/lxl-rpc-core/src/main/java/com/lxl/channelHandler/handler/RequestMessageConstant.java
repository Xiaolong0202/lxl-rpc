package com.lxl.channelHandler.handler;

import java.nio.charset.StandardCharsets;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/16  17:53
 **/
public interface RequestMessageConstant {
    public static final byte[] MAGIC_NUM =  "lxlRpc".getBytes(StandardCharsets.UTF_8);
    public static final byte VERSION = 1;
    public static final short HEAD_LENGTH = 28;
}
