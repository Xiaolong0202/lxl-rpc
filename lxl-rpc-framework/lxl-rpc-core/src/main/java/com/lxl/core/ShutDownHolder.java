package com.lxl.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/29  21:53
 **/
public class ShutDownHolder {


    /**
     * 挡板
     */
    public static AtomicBoolean baffle = new AtomicBoolean(false);

    /**
     *用于请求的计数器
     */
    public static volatile AtomicLong requestCount = new AtomicLong(0);
}
