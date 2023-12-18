package com.lxl.impl;

import com.lxl.GreetingsService;
import com.lxl.annotation.LxlRpcApi;

@LxlRpcApi(group = "primary")
public class GreetingsServiceImpl implements GreetingsService {
    @Override
    public String sayHello(String name) {
        return "hi  "+name;
    }

    @Override
    public String add(int a, int b) {
        return String.valueOf(a+b);
    }
}
