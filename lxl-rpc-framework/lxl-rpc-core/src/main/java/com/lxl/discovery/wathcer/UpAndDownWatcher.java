package com.lxl.discovery.wathcer;

import com.lxl.LxlRpcBootStrap;
import com.lxl.NettyClientBootStrapInitializer;
import com.lxl.loadbalance.LoadBalancer;
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
            List<InetSocketAddress> inetSocketAddressList = LxlRpcBootStrap.getInstance().getConfiguration().getRegistryConfig().getRegistry().lookup(serviceName);
            //处理上线的结点，
            for (InetSocketAddress inetSocketAddress : inetSocketAddressList) {
                //新增的结点
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
            //处理下线的结点,在Channel_cache当中但是不在
            LxlRpcBootStrap.CHANNEL_CACHE.forEach(((inetSocketAddress, channel) -> {
                //不存在inetSocketAddressList当中的话,则需要删除该结点|
                if (!inetSocketAddressList.contains(inetSocketAddress)){
                    LxlRpcBootStrap.CHANNEL_CACHE.remove(inetSocketAddress);
                    if(log.isDebugEnabled()){
                        log.debug("服务节点【{}】已经下线",inetSocketAddress);
                    }
                }
            }));
            //调用reloadBanlance方法
            //获得负载均衡器,进行重新的reBalance
            LoadBalancer loadBalancer = LxlRpcBootStrap.getInstance().getConfiguration().getLoadBalancer();
            loadBalancer.reLoadBalance(serviceName,inetSocketAddressList);
        }
    }

  private String  getServiceName(String eventPath){
      String[] split = eventPath.split("/");
      return split[split.length-1];
  }
}
