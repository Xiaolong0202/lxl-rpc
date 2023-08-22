package com.lxl;

import com.lxl.discovery.RegistryConfig;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.SerializeType;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.loadbalance.impl.RoundLoadBalancer;
import lombok.Data;

/**
 * 全局的配置类： 代码配置-->xml配置-->spi配置-->默认项
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/23  1:18
 **/
@Data
public class Configuration {
        //配置信息-->端口号
        private    int PORT = 8094;

        //应用程序的名字
        private String appName = "default";

        private ProtocolConfig protocolConfig;
        //注册配置
        private RegistryConfig registryConfig;

        //配置信息--ID生成器
        private   IdGenerator ID_GENERATOR = new IdGenerator(1,2);
        //序列化的类型
        private   SerializeType serializeType = SerializeType.JDK;
        //压缩的类型
        private   CompressType compressType = CompressType.GZIP;
        //负载均衡策略
        private LoadBalancer loadBalancer = new RoundLoadBalancer();

        //读取xml


        //进行配置

        public Configuration(){

        }

}
