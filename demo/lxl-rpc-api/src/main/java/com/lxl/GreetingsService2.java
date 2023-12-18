package com.lxl;

import com.lxl.annotation.ReTry;

public interface GreetingsService2 {
    String sayHello(String name);
   @ReTry(tryTimes = 10,intervalTime = 2)
    String add(int a,int b);
}
