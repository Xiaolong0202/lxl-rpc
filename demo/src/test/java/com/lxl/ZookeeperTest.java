package com.lxl;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import zookeeper.MyWathcer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ZookeeperTest {
    ZooKeeper zooKeeper;

    @BeforeEach
    public void before(){
        try {
            zooKeeper = new ZooKeeper("39.107.52.125:2181",10000,new MyWathcer());//默认的监听器
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void test1() throws UnsupportedEncodingException, InterruptedException, KeeperException {
            //创建节点
        String res = zooKeeper.create("/lxl/sons/ty", "我的大儿子汤禹".getBytes("UTF-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(res);
    }

    @Test
    void test2() throws Exception{
        zooKeeper.delete("/lxl/sons",-1);//-1是直接无视版本号
    }
    @Test
    void test3() throws Exception{
        Stat stat = zooKeeper.exists("/lxl", true);//检查版本号,填写TRUE的话就会使用那个默认的watcher
        int version = stat.getVersion();//当前节点·1的数据版本
        System.out.println("version = " + version);
        int aversion = stat.getAversion();//当前节点的ACL数据版本
        System.out.println("aversion = " + aversion);
        int cversion = stat.getCversion();//当前节点的子节点的数据版本
        System.out.println("cversion = " + cversion);
        while (true){
            Thread.sleep(1000L);
        }

    }
}
