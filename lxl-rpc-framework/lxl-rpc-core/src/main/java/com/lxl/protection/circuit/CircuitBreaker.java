package com.lxl.protection.circuit;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 异常比例熔断器
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/28  17:27
 **/
public class CircuitBreaker {

    //断路器的三种状态, open close half_open
    //收集指标根据指标来决定是否要熔断

    private volatile boolean isOpen = false;


    //总的请求数
    private AtomicInteger requestCount = new AtomicInteger(0);


    private AtomicInteger errorRequestCount = new AtomicInteger(0);


    /**
     *  异常比例阈值
     */
    private final double maxErrorRate;

    /**
     * 即允许通过的最小请求数，在该数量内不发生熔断
     */
    private final int minimumRequests;

    public CircuitBreaker(double maxErrorRate,int minimumRequests) {
        this.maxErrorRate = maxErrorRate;
        this.minimumRequests = minimumRequests;
    }

    /**
     * 重置熔断器
     */
    public void reset() {
        this.requestCount.set(0);
        this.errorRequestCount.set(0);
        this.isOpen = false;
    }


    public void countRequest() {
        this.requestCount.incrementAndGet();
    }

    public void countErrorRequest() {
        this.errorRequestCount.incrementAndGet();
    }

    public boolean isBreak() {
        if (!this.isOpen) {
            if (errorRequestCount.get() > 0 && requestCount.get() > 0  && requestCount.get() >= minimumRequests
                    && (double) errorRequestCount.get() / (double) requestCount.get() > maxErrorRate){
                this.isOpen =true;
            }
        }
        return this.isOpen;
    }
}
