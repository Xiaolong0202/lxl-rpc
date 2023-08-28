package com.lxl.protection;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
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


    private final int maxErrorRequestCount;

    private final double maxErrorRate;

    /**
     * 通过请求率而导致熔断,所需要的最小请求数
     */
    private final int stopByRateMinRequestNum;

    public CircuitBreaker(int maxErrorRequestCount, double maxErrorRate,int stopByRateMinRequestNum) {
        this.maxErrorRequestCount = maxErrorRequestCount;
        this.maxErrorRate = maxErrorRate;
        this.stopByRateMinRequestNum = stopByRateMinRequestNum;
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

    public boolean isOpen() {
        if (!this.isOpen) {
            if (errorRequestCount.get() >= maxErrorRequestCount) {
                this.isOpen = true;
            }
            if (errorRequestCount.get() > 0 && requestCount.get() > 0  && requestCount.get() >= stopByRateMinRequestNum
                    && (double) errorRequestCount.get() / (double) requestCount.get() > maxErrorRate){
                this.isOpen =true;
            }
        }
        return this.isOpen;
    }
}
