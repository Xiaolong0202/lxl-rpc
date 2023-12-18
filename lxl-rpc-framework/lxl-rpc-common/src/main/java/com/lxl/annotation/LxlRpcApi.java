package com.lxl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被标注了该注解的类会被扫包扫上
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/23  0:50
 **/

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LxlRpcApi {

    //服务的分组名称
    String group() default "default";
}
