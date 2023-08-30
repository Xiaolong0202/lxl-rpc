package com.lxl;

import com.lxl.utils.zookeeper.ZookeeperNode;
import com.lxl.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;



@Slf4j
public class Application {
    public static void main(String[] args) {

        String connectString = "127.0.0.1:2181";
        int timeout = Constant.DEFAULT_ZK_TIME_OUT;
        //创建基本的目录
            try (ZooKeeper zooKeeper = ZookeeperUtil.createZookeeper(connectString,timeout)) {
                //定义节点和数据
                String basePath = "/lxlRpc-metadata";
                String providerPath = basePath + "/provider";
                String  consumerPath = basePath + "/consumer";
                ZookeeperNode baseNode = new ZookeeperNode(basePath,"basePath".getBytes("UTF-8"));
                ZookeeperNode providerNode = new ZookeeperNode(providerPath, "provider".getBytes("UTF-8"));
                ZookeeperNode consumerNode= new ZookeeperNode(consumerPath,"consumerPath".getBytes("UTF-8"));
                List.of(baseNode,providerNode,consumerNode).forEach(node->{
                    ZookeeperUtil.createZookeeperNode(zooKeeper,node,null,CreateMode.PERSISTENT);
                   });
            } catch (IOException | InterruptedException  e) {
                log.error("创建结点的时候产生了异常如下:",e);
            throw new RuntimeException(e);
        }
    }
}
