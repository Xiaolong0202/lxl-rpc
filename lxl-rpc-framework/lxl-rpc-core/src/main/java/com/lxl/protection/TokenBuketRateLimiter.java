package com.lxl.protection;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于令牌桶算法的限流器,以固定速率向容器当中放入令牌，若没有则拒绝访问
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/27  2:19
 **/
public class TokenBuketRateLimiter {

    //令牌的数量
    private int tokens;

    //限流的本质就是令牌数
    private final int capacity;

    //令牌桶算法的令牌如果没有了怎么办？按照一定的速率给令牌添加令牌,不能超过总数
    private final int rate;

    //上一次放令牌的时间
    private long lastTokenTime;

    /**
     * 判断请求是否可以放行
     *
     * @param rate
     * @param capacity
     */
    public TokenBuketRateLimiter(int rate, int capacity) {
        this.rate = rate;
        this.capacity = capacity;
        this.lastTokenTime = System.currentTimeMillis();
        this.tokens = capacity;
    }

    /**
     * @return
     */
    public synchronized boolean allowRequest() {
        long currentTimes = System.currentTimeMillis();
        long timeInterval = currentTimes - lastTokenTime;
        if (timeInterval >= 1000 / rate) {//如果间隔时间可以使得计算出来的所需要添加的token数不为0即可
            int needAddTokens = (int) (timeInterval * rate / 1000);//按照速率计算出需要添加的令牌数
            tokens = Math.min(capacity, tokens + needAddTokens);//添加令牌并保证不超出容量

            this.lastTokenTime = System.currentTimeMillis();
        }

        //获取令牌，若令牌桶当中有令牌则放行，否则则拦截
        if (tokens > 0) {
            tokens--;
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TokenBuketRateLimiter rateLimiter = new TokenBuketRateLimiter(10, 10);
        rateLimiter.allowRequest();
        for (int i = 0; i < 1000; i++) {
            Thread.sleep(10);
            System.out.println("rateLimiter.allowRequest() = " + rateLimiter.allowRequest());
        }
    }
}
