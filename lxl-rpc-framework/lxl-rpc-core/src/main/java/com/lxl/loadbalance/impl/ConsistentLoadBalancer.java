package com.lxl.loadbalance.impl;

import com.lxl.LxlRpcBootStrap;
import com.lxl.loadbalance.AbstractLoadBalancer;
import com.lxl.loadbalance.Selector;
import com.lxl.transport.message.request.LxlRpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/19  14:17
 **/
@Slf4j
public class ConsistentLoadBalancer extends AbstractLoadBalancer {


    @Override
    public Selector getSelector(List<InetSocketAddress> inetSocketAddressList) {
        return new ConsistentSelector(inetSocketAddressList, 128);
    }

    private static class ConsistentSelector implements Selector {

        private int virtualNodeNum;

        private SortedMap<Integer, InetSocketAddress> hashRing;

        public ConsistentSelector(List<InetSocketAddress> inetSocketAddressList, int virtualNodeNum) {
            this.virtualNodeNum = virtualNodeNum;
            hashRing = Collections.synchronizedSortedMap(new TreeMap<>());
            inetSocketAddressList.forEach(this::addNodeToRing);
        }

        private void addNodeToRing(InetSocketAddress inetSocketAddress) {
            String socketAddressString = inetSocketAddress.toString();
            for (int i = 0; i < virtualNodeNum; i++) {
                Integer key = getHash(socketAddressString + "-" + i);
                hashRing.put(key, inetSocketAddress);
                if (log.isDebugEnabled()) {
                    log.debug("hash为【{}】的结点已经被挂载到了哈希环上挂载了", key);
                }
            }
        }

        private Integer getHash(String socketAddressString) {
            int hashRes = 0;
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] digested = md.digest(socketAddressString.getBytes());
                for (int i = 0; i < 4; i++) {
                    hashRes <<= hashRes;
                    hashRes = hashRes | (digested[i] & 255);
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return hashRes;
        }

        @Override
        public InetSocketAddress nextServiceAddr() {
            LxlRpcRequest lxlRpcRequest = LxlRpcBootStrap.REQUEST_THREAD_LOCAL.get();
            String reqIdStr = Long.toString(lxlRpcRequest.getRequestId());
            Integer hashCode = getHash(reqIdStr);
            //首先判断该hash值是否能够刚好坐落在一个服务器上面，苟泽就去寻找最近的一个结点
            if (!hashRing.containsKey(hashCode)) {
                SortedMap<Integer, InetSocketAddress> tailMap = hashRing.tailMap(hashCode);
                //若tailMap没有叶子结点了则返回哈希环的第一个值，否则则返回tailMap最小值
                hashCode = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();
            }
            return hashRing.get(hashCode);
        }
    }
}
