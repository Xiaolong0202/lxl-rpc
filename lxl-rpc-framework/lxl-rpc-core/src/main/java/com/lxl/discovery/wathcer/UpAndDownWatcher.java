package com.lxl.discovery.wathcer;

import com.lxl.LxlRpcBootStrap;
import com.lxl.NettyClientBootStrapInitializer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/22  20:13
 **/
@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        System.out.println(event.getType());
        if (event.getType() == Event.EventType.NodeChildrenChanged){
            if (log.isDebugEnabled())log.debug("检测到有服务上|下线");
            String serviceName  = getServiceName(event.getPath());
            List<InetSocketAddress> inetSocketAddressList = LxlRpcBootStrap.getInstance().getRegistry().lookup(serviceName);
            for (InetSocketAddress inetSocketAddress : inetSocketAddressList) {
                //新增的结点
                //下线的结点
                Channel channel = null;
                if (!LxlRpcBootStrap.CHANNEL_CACHE.containsKey(inetSocketAddress)){
                    try {
                      channel = NettyClientBootStrapInitializer.getBootstrap().connect(inetSocketAddress).sync().channel();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    //放入缓存当中
                    LxlRpcBootStrap.CHANNEL_CACHE.put(inetSocketAddress,channel);
                    if (log.isDebugEnabled()){
                        log.debug("添加了一个新节点【{}】",inetSocketAddress);
                    }
                }
            }
            System.out.println("子节点产生了变化");
            //如果产生变更
        }
    }

  private String  getServiceName(String eventPath){
      String[] split = eventPath.split("/");
      return split[split.length-1];
  }
}
