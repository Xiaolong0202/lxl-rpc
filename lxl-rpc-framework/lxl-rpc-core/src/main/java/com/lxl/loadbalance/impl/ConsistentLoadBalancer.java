package com.lxl.loadbalance.impl;

import com.lxl.loadbalance.AbstractLoadBalancer;
import com.lxl.loadbalance.Selector;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/19  14:17
 **/
public class ConsistentLoadBalancer extends AbstractLoadBalancer {
    @Override
    public Selector getSelector(List<InetSocketAddress> inetSocketAddressList) {
        return new ConsistentSelector(inetSocketAddressList);
    }

    private static class ConsistentSelector implements Selector{


        public ConsistentSelector(List<InetSocketAddress> inetSocketAddressList) {
            inetSocketAddressList.forEach(inetSocketAddress -> {
                String socketAddressString = inetSocketAddress.toString();
            });
        }

        @Override
        public InetSocketAddress nextServiceAddr() {
            return ;
        }
    }
}
