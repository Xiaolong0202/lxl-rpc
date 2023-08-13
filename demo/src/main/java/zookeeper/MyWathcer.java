package zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class MyWathcer implements Watcher {

    @Override
    public void process(WatchedEvent event) {
        //需要判断事件的类型
        if (event.getType() == Event.EventType.None){
            //判断是否是连接类型的时间,None与连接有关
            if (event.getState()==Event.KeeperState.SyncConnected){
                System.out.println("zookeeper连接成功");
            }else if (event.getState()==Event.KeeperState.AuthFailed){
                System.out.println("zookeeper认证失败");
            }else if (event.getState()==Event.KeeperState.Disconnected){
                System.out.println("断开连接");
            }
        }else if (event.getType()== Event.EventType.NodeCreated){
            System.out.println(event.getPath()+"： 节点创建");
        }else if (event.getType()== Event.EventType.NodeDeleted){
            System.out.println(event.getPath()+"： 节点删除");
        }
    }
}
