package com.lxl.discovery.impl;

import com.lxl.Constant;
import com.lxl.ServiceConfig;
import com.lxl.discovery.AbstractRegistry;
import com.lxl.exceptions.DiscoveryException;
import com.lxl.exceptions.NetWorkException;
import com.lxl.utils.net.NetUtil;
import com.lxl.utils.zookeeper.ZookeeperNode;
import com.lxl.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {

    private ZooKeeper zooKeeper;

    public ZookeeperRegistry(String connectUrl,int timeout) {
        this.zooKeeper = ZookeeperUtil.createZookeeper(connectUrl,timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {

        //服务名称的结点，是一个持久的结点
        String parentNode = Constant.BASE_PROVIDER+'/'+service.getInterface().getName();
        ZookeeperUtil.createZookeeperNode(zooKeeper,new ZookeeperNode(parentNode,null),null, CreateMode.PERSISTENT);
        if (log.isDebugEnabled()){
            log.debug("服务{},已经被注册",service.getInterface().getName());
        }
        //创建本机的临时节点，也就是ip地址什么的, ip:port,先写死8088
        //服务提供方的端口一般自己设定
        //ip我们使用的是局域网ip
        String ipAddr =  NetUtil.getLocalHostExactAddress();
        if (log.isDebugEnabled())log.debug("局域网ip地址:"+ipAddr);
        //TODO  后续处理端口的问题
        String node = parentNode+'/'+ipAddr+':'+8088;
        ZookeeperUtil.createZookeeperNode(zooKeeper,new ZookeeperNode(node,null),null,CreateMode.EPHEMERAL);//创建一个临时的结点
    }

    @Override
    public InetSocketAddress lookup(String serviceName) {
        //找到对应的结点
        String serviceNode = Constant.BASE_PROVIDER+'/'+serviceName;
        //从zk中获取它的子节点
        List<String> children = ZookeeperUtil.getChildren(zooKeeper,serviceNode,null);
        //将其封装成InetSocketAddress
        List<InetSocketAddress> inetSocketAddressList = children.stream().map(ipAndPort -> {
            String[] split = ipAndPort.split(":");
            String ipAddr = split[0];
            int port = Integer.parseInt(split[1]);
            return new InetSocketAddress(ipAddr, port);
        }).toList();
        if (inetSocketAddressList.size() == 0){
            throw new DiscoveryException("未发现任何可用的主机");
        }

        return inetSocketAddressList.get(0);
    }
}
