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

    public static final int VERSION_LENGTH = 1;
    public static final int LENGTH_FIELD_OFFSET = MAGIC_NUM.length+VERSION_LENGTH+Short.SIZE/8;//
    public static final int LENGTH_FIELD_LENGTH = Long.SIZE/8;
}
