package com.lxl.config.resolver;

import com.lxl.compress.Compressor;
import com.lxl.config.Configuration;
import com.lxl.factory.CompressFactory;
import com.lxl.factory.ObjectMapper;
import com.lxl.factory.SerializerFactory;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.serialize.Serializer;
import com.lxl.spi.SpiHandler;

import java.util.List;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/24  23:40
 **/
public class SpiResolver {
    public void loadFromSpi(Configuration configuration) {
        //从SPI当中获取对应的实例
        List<ObjectMapper<LoadBalancer>> loadbalancerWrapperList = SpiHandler.getList(LoadBalancer.class);
        List<ObjectMapper<Compressor>> compressorWrapperList = SpiHandler.getList(Compressor.class);
        List<ObjectMapper<Serializer>> serializerWrapperList = SpiHandler.getList(Serializer.class);
        //todo 将这些实现都put到工厂当中
        //将获取到的实例加载到configuratoin当中
        if (loadbalancerWrapperList != null && loadbalancerWrapperList.size() > 0) {
            configuration.setLoadBalancer(loadbalancerWrapperList.get(0).getImplement());
        }
        if (compressorWrapperList != null && compressorWrapperList.size() > 0) {
            compressorWrapperList.forEach(CompressFactory::addCompressor);
            configuration.setCompressType(compressorWrapperList.get(0).getName());
        }
        if (serializerWrapperList != null && serializerWrapperList.size() > 0) {
            serializerWrapperList.forEach(SerializerFactory::addSerializer);
            configuration.setSerializeType(serializerWrapperList.get(0).getName());
        }
        System.out.println();
    }
}
