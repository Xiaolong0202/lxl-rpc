package com.lxl.loadbalance.impl;

import com.lxl.discovery.Registry;
import com.lxl.exceptions.LoadBalanceException;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.loadbalance.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/18  21:20
 **/
public class RoundLoadBalancer implements LoadBalancer {

    private Registry registry;

    //服务与选择器的映射，因为一个选择器就维护了一个服务列表
    private final  Map<String,Selector> SELECTOR_CACHE = new ConcurrentHashMap<>(8);

    public RoundLoadBalancer(Registry registry) {
        this.registry = registry;
    }

    @Override
    public InetSocketAddress selectServiceAddr(String serviceName) {
        Selector selector = SELECTOR_CACHE.get(serviceName);
        if (selector == null){
            SELECTOR_CACHE.put(serviceName,new BalancerSelector(registry.lookup(serviceName)));
            return selectServiceAddr(serviceName);
        }
        return selector.nextServiceAddr();
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
