package com.lxl.loadbalance;

import java.net.InetSocketAddress;
import java.util.List;

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

    /**
     * 当感知节点发生了动态上下线的时候，我们需要重新进行负载均衡
     *
     * @param serviceName
     * @param inetSocketAddressList
     */
    void reLoadBalance(String serviceName,List<InetSocketAddress> inetSocketAddressList);
}
