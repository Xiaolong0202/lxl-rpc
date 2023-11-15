package com.lxl.config;

import com.lxl.ProtocolConfig;
import com.lxl.compress.Compressor;
import com.lxl.compress.impl.GzipCompressImpl;
import com.lxl.config.resolver.SpiResolver;
import com.lxl.config.resolver.XMLResolver;
import com.lxl.core.IdGenerator;
import com.lxl.discovery.RegistryConfig;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.SerializeType;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.loadbalance.impl.RoundLoadBalancer;
import com.lxl.serialize.Serializer;
import com.lxl.serialize.impl.HessianSerializerImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局的配置类： 代码配置-->xml配置-->spi配置-->默认项
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/23  1:18
 **/
@Data
@Slf4j
public class Configuration {
    //该类是BootStrap的一个final成员变量，所以会在BootStrap被加载的时候被实例化,所以能够保证SpiResolver、XMLResolver这些读取配置的方法能够在第一时间被执行
    //配置信息-->端口号
    private int PORT = 8080;


    //应用程序的名字
    private String appName = "default";

    //注册配置
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

    //配置信息--ID生成器
    private IdGenerator idGenerator = new IdGenerator(1L, 2L);
    //序列化的类型
    private String serializeType = "JDK";

    //压缩的类型
    private String compressType = "GZIP";

    //负载均衡策略
    private LoadBalancer loadBalancer = new RoundLoadBalancer();



    //进行配置
    public Configuration() {
        //成员变量默认配置项


        //spi发现配置
        SpiResolver sprResolver = new SpiResolver();
        sprResolver.loadFromSpi(this);

        //读取xml配置信息
        XMLResolver xmlResolver = new XMLResolver();
        xmlResolver.loadFromXml(this);

        //编程配置项
    }

    public static void main(String[] args) {
        new Configuration();
    }


}
