package com.lxl.loadbalance;

import java.net.InetSocketAddress;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/18  20:28
 **/
public interface LoadBalancer {

    /**
     * 根据服务名
     * @return
     */
    InetSocketAddress selectServiceAddr(String ServiceName);
}
