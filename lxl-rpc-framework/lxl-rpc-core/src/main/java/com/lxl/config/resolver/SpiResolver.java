package com.lxl.config.resolver;

import com.lxl.config.Configuration;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.spi.SpiHandler;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/24  23:40
 **/
public class SpiResolver {
    public void loadFromSpi(Configuration configuration) {
        LoadBalancer loadBalancer = SpiHandler.get(LoadBalancer.class);

    }
}
