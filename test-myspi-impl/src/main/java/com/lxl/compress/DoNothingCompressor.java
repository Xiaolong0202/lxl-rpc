package com.lxl.compress;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/25  15:17
 **/
@Slf4j
public class DoNothingCompressor implements Compressor{
    @Override
    public byte[] compress(byte[] bytes) {
        log.debug("什么都没有压缩");
        return bytes;
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        log.debug("什么都没有压缩");
        return bytes;
    }
}
