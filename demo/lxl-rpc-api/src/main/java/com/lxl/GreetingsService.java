package com.lxl;

import com.lxl.annotation.ReTry;

public interface GreetingsService {
    String sayHello(String name);

    @ReTry
    String add(int a,int b);
}
