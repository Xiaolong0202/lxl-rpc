package com.lxl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该类标注在接口的抽象方法当中，如果需要重试则标注在该方法中
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/26  18:46
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReTry {
    /**
     * 重试次数
     * @return
     */
    int tryTimes() default 4;

    /**
     * 重试的间隔时间
     * @return
     */
    int intervalTime() default 3000;
}
