package com.lxl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求ID生成器
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/17  16:05
 **/
public class IdGenerator {

    //雪花算法 -- 世界上没有一片雪花是相同的
    //机房号  5bit
    //机器号  5bit
    //时间戳  从long表示改为用42位数的 表示  所以表示的时间有限我们需要换一个起始的日期，比如说可以换成公司创立的时间
    //同一个机房的同一个机器号的同一时间可能因为并发量很大需要多个id
    //序列号  12bit
    // 5+5+32+12 = 64 bit

    //起始的时间戳
    public static final long START_TIME_STAMP;
    //机房号
    public static final long DATA_CENTER_BIT = 5;
    //机器号
    public static final long MACHINE_BIT = 5;
    //
    public static final long SEQUENCE_BIT = 12;

    //最大值
    public static final long MAX_MACHINE_VALUE = 1 << MACHINE_BIT - 1;
    public static final long MAX_DATA_CENTER_VALUE = 1 << DATA_CENTER_BIT - 1;
    public static final long MAX_SEQUENCE_VALUE = 1 << SEQUENCE_BIT - 1;

    //时间戳 42  机房号 5 机器号 5  序列号 12
    public static final long TIME_STAMP_LEFT = MACHINE_BIT + DATA_CENTER_BIT + SEQUENCE_BIT;
    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;
    public static final long MACHINE_LEFT = SEQUENCE_BIT;


    static {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date startDate = simpleDateFormat.parse("2023-01-01 00:00:00");
            START_TIME_STAMP = startDate.getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    private long dataCenterId;
    private long machineId;
    private AtomicLong sequenceId = new AtomicLong(0);//使用原子类来保证线程安全

    //时钟回拨问题,发现本地的时间比时间服务器块则需要将时间戳回调，这样会出现问题
    private long lastTimeStamp = -1L;

    public IdGenerator(long dataCenterId, long machineId) {
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
        //如果传入的值大于所允许的最大值
        if (dataCenterId > MAX_DATA_CENTER_VALUE) {
            throw new RuntimeException("传入的机房ID值过大");
        }
        if (machineId > MAX_MACHINE_VALUE) {
            throw new RuntimeException("传入的机器ID值过大");
        }


    }

    public long getId() {
        long currentTimeMillis = System.currentTimeMillis();
        long timeStamp = currentTimeMillis - START_TIME_STAMP;//获取自定义的时间戳

        if (timeStamp  <  lastTimeStamp){
            throw new RuntimeException("您的服务器进行了时钟回调");
        }

        //如果是同一个时间节点就需要做相应的处理
        if (timeStamp == lastTimeStamp){
            sequenceId.incrementAndGet();//i++
            if (sequenceId.get() == MAX_SEQUENCE_VALUE){
                return getId();
            }
        }else {
            sequenceId.getAndSet(0);
        }
        lastTimeStamp = timeStamp;
        return timeStamp << TIME_STAMP_LEFT | dataCenterId << DATA_CENTER_LEFT | machineId << MACHINE_LEFT | sequenceId.get();
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1L,2L);
        for (int i = 0; i < 10000; i++) {
            new Thread(()->{
                System.out.println("idGenerator.getId() = " + idGenerator.getId());
            }).start();
        }
    }
}
