package com.lxl.loadbalance;

import com.lxl.LxlRpcBootStrap;
import com.lxl.discovery.Registry;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/19  0:30
 **/
public abstract class AbstractLoadBalancer implements LoadBalancer{

    //服务与选择器的映射，因为一个选择器就维护了一个服务列表
    private final Map<String,Selector> SELECTOR_CACHE = new ConcurrentHashMap<>(8);


    @Override
    public InetSocketAddress selectServiceAddr(String serviceName) {
        Registry registry = LxlRpcBootStrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        Selector selector = SELECTOR_CACHE.get(serviceName);
        if (selector == null){
            selector = getSelector(registry.lookup(serviceName));//获取对应的selector实现类，由具体的selector去做均衡负载策略
            SELECTOR_CACHE.put(serviceName,selector);
            return selectServiceAddr(serviceName);
        }
        return selector.nextServiceAddr();
    }

    @Override
    public void reLoadBalance(String serviceName, List<InetSocketAddress> inetSocketAddressList) {
        SELECTOR_CACHE.put(serviceName,getSelector(inetSocketAddressList));
    }

    public abstract Selector getSelector(List<InetSocketAddress> inetSocketAddresses);
}
