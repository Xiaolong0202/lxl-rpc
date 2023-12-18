package com.lxl.impl;

import com.lxl.GreetingsService;
import com.lxl.GreetingsService2;
import com.lxl.annotation.LxlRpcApi;

@LxlRpcApi
public class GreetingsServiceImpl2 implements GreetingsService2 {
    @Override
    public String sayHello(String name) {
        return "hi  "+name;
    }

    @Override
    public String add(int a, int b) {
        return String.valueOf(a+b);
    }
}
