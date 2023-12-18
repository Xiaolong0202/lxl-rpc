package com.lxl.protection.rateLimite;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/28  21:29
 **/
public interface RateLimiter {
    boolean allowRequest();
}
