package com.lxl.utils;

import com.lxl.Constant;
import com.lxl.exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;


@Slf4j
public class ZookeeperUtil {


    /**
     * 使用默认的配置，返回一个创建好的Zookeeper
     * @return zookeeper实例
     */
    public static ZooKeeper createZookeeper(){
        return createZookeeper(Constant.DEFAULT_ZK_CONNECT,Constant.DEFAULT_ZK_TIME_OUT);
    }

    /**
     * 使用默认的配置，返回一个创建好的Zookeeper
     * @param connectString
     * @param timeout
     * @return zookeeper实例
     */
    public static ZooKeeper createZookeeper(String connectString,int timeout){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ZooKeeper zooKeeper;
        try {
            zooKeeper  = new ZooKeeper(connectString, timeout, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.debug("客户端连接成功");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("创建Zookeeper实例的时候发生了异常:",e);
            throw new ZookeeperException();
        }
    }
}
