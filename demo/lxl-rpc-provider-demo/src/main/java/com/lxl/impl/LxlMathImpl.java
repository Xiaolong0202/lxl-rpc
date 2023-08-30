package com.lxl.impl;

import com.lxl.LxlMath;
import com.lxl.annotation.LxlRpcApi;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/30  16:00
 **/
@LxlRpcApi
public class LxlMathImpl implements LxlMath {
    @Override
    public int add(int a, int b) {
        return a+b;
    }

    @Override
    public int multiplication(int a, int b) {
        return a*b;
    }
}
