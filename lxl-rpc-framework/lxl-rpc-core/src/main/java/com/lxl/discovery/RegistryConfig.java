package com.lxl.discovery;

import com.lxl.Constant;
import com.lxl.discovery.impl.NacosRegistry;
import com.lxl.discovery.impl.ZookeeperRegistry;
import com.lxl.exceptions.DiscoveryException;

public class RegistryConfig {

    //定义连接的url
    private String connectString;

    private Registry registry = null;
    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }


    /**
     * 使用简单工厂的设计模式返回，所需要的注册中心实例
     * @return
     */
    public Registry getRegistry() {
        if (this.registry != null) return this.registry;
        String lowerCaseRegistryType = getLowerCaseRegistryType(connectString,true);
        if ("zookeeper".equals(lowerCaseRegistryType)){
            return new ZookeeperRegistry(getLowerCaseRegistryType(connectString,false), Constant.DEFAULT_ZK_TIME_OUT);
        }else if ("nacos".equals(lowerCaseRegistryType)){
            return new NacosRegistry(getLowerCaseRegistryType(connectString,false));
        }
        throw new DiscoveryException("未发现合适的注册中心");//如果找不到合适的注册心，则抛出异常
    }

    public String getLowerCaseRegistryType(String connectString,boolean ifReturnType){
        String[] split = connectString.split("://");
        if (ifReturnType){
            return split[0].toLowerCase().trim();
        }else {
         return   split[1].toLowerCase().trim();
        }
    }
}
