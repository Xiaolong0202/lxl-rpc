package com.lxl.discovery;


import com.lxl.ServiceConfig;

public interface Registry {

    /**
     * 将服务注册到
     * @param serviceConfig
     */
    void register(ServiceConfig<?> serviceConfig);
}
