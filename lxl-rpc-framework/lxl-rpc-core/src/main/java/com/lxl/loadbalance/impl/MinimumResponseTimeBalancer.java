package com.lxl.loadbalance.impl;

import com.lxl.LxlRpcBootStrap;
import com.lxl.loadbalance.AbstractLoadBalancer;
import com.lxl.loadbalance.Selector;
import com.lxl.transport.message.request.LxlRpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ThreadInfo;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 最短响应时间负载均衡器
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/19  14:17
 **/
@Slf4j
public class MinimumResponseTimeBalancer extends AbstractLoadBalancer {


    @Override
    public Selector getSelector(List<InetSocketAddress> inetSocketAddressList) {
        return new MinimumResponseTimeSelector(inetSocketAddressList);
    }

    private static class MinimumResponseTimeSelector implements Selector {
        Set<InetSocketAddress> inetSocketAddressSet;


        public MinimumResponseTimeSelector(List<InetSocketAddress> inetSocketAddressList) {
            this.inetSocketAddressSet = new CopyOnWriteArraySet<>(inetSocketAddressList);
        }

        @Override
        public InetSocketAddress nextServiceAddr() {
            LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.forEach((k, v) -> {
                System.out.println("k = " + k);
                System.out.println("v = " + v);
            });
            InetSocketAddress res = inetSocketAddressSet.iterator().next();
            if (LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.size() <= 0) {//若没有初始化好则从缓存当中拿一个
                return res;
            }
            //从LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE当中找到第一个属于在set当中的inetsocketAddress说明是最大的
            Long firstedKey = LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.firstKey();
            for (Map.Entry<Long, InetSocketAddress> longInetSocketAddressEntry : LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.entrySet()) {
                if (inetSocketAddressSet.contains(longInetSocketAddressEntry.getValue())) {
                    res = longInetSocketAddressEntry.getValue();
                    firstedKey = longInetSocketAddressEntry.getKey();
                    break;
                }
            }
            if (!LxlRpcBootStrap.CHANNEL_CACHE.containsKey(res)) {
                //如果channel缓存当中没有包含该InetSocketList的channel,则从treeMap当中删除
                log.error("如果channel缓存当中没有包含该【{}】的channel,则从treeMap当中删除", res);
                LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.remove(firstedKey);
                return nextServiceAddr();
            }
            return res;
        }


    }
}
