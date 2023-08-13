package com.lxl;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;



@Slf4j
public class Application {
    public static void main(String[] args) {

        CountDownLatch countDownLatch = new CountDownLatch(1);


        String connectString = "39.107.52.125:2181";
        int timeout = Constant.DEFAULT_ZK_TIME_OUT;
        //创建基本的目录

            try (ZooKeeper zooKeeper = new ZooKeeper(connectString, timeout, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.debug("客户端连接成功");
                    countDownLatch.countDown();
                }
            })) {
                countDownLatch.await();
                //定义节点和数据
                String basePath = "/lxlRpc-metadata";
                String providerPath = basePath + "/provider";
                String consumerPath = basePath + "/consumer";
                //先要判断是否存在,再决定是否创建节点
                if (zooKeeper.exists(basePath,null) == null){
                   String res =  zooKeeper.create(basePath, "basePath".getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                   log.info("节点【{}】,已经创建成功",res);
                }
                if (zooKeeper.exists(providerPath,null)==null){
                    String res =  zooKeeper.create(providerPath, "provider".getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    log.info("节点【{}】,已经创建成功",res);
                }
                if (zooKeeper.exists(consumerPath,null) == null){
                    String res =  zooKeeper.create(consumerPath, consumerPath.getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);//节点对所有人都开放
                    log.info("节点【{}】,已经创建成功",res);
                }
            } catch (IOException | InterruptedException | KeeperException e) {
                log.error("产生了异常如下:",e);
            throw new RuntimeException(e);
        }
    }
}
