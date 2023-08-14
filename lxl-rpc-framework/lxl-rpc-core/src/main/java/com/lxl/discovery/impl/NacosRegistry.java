package com.lxl.discovery.impl;

import com.lxl.ServiceConfig;
import com.lxl.discovery.Registry;

import java.net.InetSocketAddress;

public class NacosRegistry implements Registry {
    public NacosRegistry(String connectString) {
    }

    @Override
    public void register(ServiceConfig<?> serviceConfig) {

    }

    @Override
    public InetSocketAddress lookup(String serviceName) {
        return null;
    }
}
