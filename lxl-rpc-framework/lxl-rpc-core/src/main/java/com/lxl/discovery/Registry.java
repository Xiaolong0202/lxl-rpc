package com.lxl.discovery;


import com.lxl.ServiceConfig;

import java.net.InetSocketAddress;

public interface Registry {

    /**
     * 将服务注册到
     * @param serviceConfig
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取一个可用的服务的地址
     * @param serviceName
     * @return
     */
    InetSocketAddress lookup(String serviceName);
}
