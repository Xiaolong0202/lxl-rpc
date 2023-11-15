package com.lxl.loadbalance.impl;

import com.lxl.exceptions.LoadBalanceException;
import com.lxl.loadbalance.AbstractLoadBalancer;
import com.lxl.loadbalance.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询 负载均衡器
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/18  21:20
 **/
public class RoundLoadBalancer extends AbstractLoadBalancer {


    @Override
    public Selector getSelector(List<InetSocketAddress> list) {
        return new BalancerSelector(list);
    }

    private static class BalancerSelector implements Selector {

        private List<InetSocketAddress> serviceAddressList;

        private final AtomicInteger index;

        public BalancerSelector(List<InetSocketAddress> serviceAddressList) {
            this.serviceAddressList = serviceAddressList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress nextServiceAddr() {
            if (serviceAddressList == null || serviceAddressList.size() == 0) {
                throw new LoadBalanceException("服务列表为空");
            }
            if (index.get() > serviceAddressList.size() - 1){
                index.set(0);
            }
            return serviceAddressList.get(index.getAndIncrement());
        }
    }
}
