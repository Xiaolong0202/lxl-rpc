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
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
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
        List<InetSocketAddress> inetSocketAddressList;

        public MinimumResponseTimeSelector(List<InetSocketAddress> inetSocketAddressList) {
            this.inetSocketAddressList = inetSocketAddressList;
        }

        @Override
        public InetSocketAddress nextServiceAddr() {
            LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.forEach((k,v)->{
                System.out.println("k = " + k);
                System.out.println("v = " + v);
            });
            if (LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.size()<=0){//若没有初始化好则从缓存当中拿一个
                return LxlRpcBootStrap.CHANNEL_CACHE.entrySet().iterator().next().getKey();
            }
            InetSocketAddress res = LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.get(LxlRpcBootStrap.RESPONSE_TIME_CHANNEL_CACHE.firstKey());
            return res;
        }
    }
}
