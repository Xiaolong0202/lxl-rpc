package com.lxl.utils.zookeeper;

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

    /**
     * 在zookeeper当中创建一个结点
     * @param zookeeper
     * @param node
     * @param watcher
     * @param mode
     * @return
     */
    public static boolean createZookeeperNode(ZooKeeper zookeeper,ZookeeperNode node,Watcher watcher,CreateMode mode){
        try {
            if (zookeeper.exists(node.getNodePath(),watcher)==null){
                String res= zookeeper.create(node.getNodePath(),node.getData(), ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
                log.info("节点【{}】,已经创建成功",res);
                return true;
            }else {
                log.info("节点【{}】,已经存在，无需再进行创建",node.getNodePath());
            }
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * c关闭zookeeper的实例
     * @param zooKeeper
     */
    public static void close(ZooKeeper zooKeeper){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper的时候发生了异常");
            throw new ZookeeperException();
        }
    }
}
