package com.lxl.impl;

import com.lxl.GreetingsService;

public class GreetingsServiceImpl implements GreetingsService {
    @Override
    public String sayHello(String name) {
        return "hi  "+name;
    }
}
