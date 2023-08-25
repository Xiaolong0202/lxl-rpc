package com.lxl.config.resolver;

import com.lxl.compress.Compressor;
import com.lxl.config.Configuration;
import com.lxl.enumnation.CompressType;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.serialize.Serializer;
import com.lxl.spi.SpiHandler;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/24  23:40
 **/
public class SpiResolver {
    public void loadFromSpi(Configuration configuration) {
        //从SPI当中获取对应的实例
        LoadBalancer loadBalancer = SpiHandler.get(LoadBalancer.class);
        Compressor compressor = SpiHandler.get(Compressor.class);
        Serializer serializer = SpiHandler.get(Serializer.class);
        //todo 将这些实现都put到工厂当中
        //将获取到的实例加载到configuratoin当中
        if (loadBalancer != null) configuration.setLoadBalancer(loadBalancer);
        if (compressor != null) configuration.setCompressor(compressor);
        if (serializer != null) configuration.setSerializer(serializer);
    }
}
