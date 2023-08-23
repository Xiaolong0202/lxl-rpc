package com.lxl.compress;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/18  16:10
 **/
public interface Compressor {


    /**
     * 将字节数组进行压缩
     * @param bytes
     * @return
     */
    byte[]  compress(byte[] bytes);

    /**
     * 将字节数组进行压缩
     * @param bytes
     * @return
     */
    byte[]  decompress(byte[] bytes);

}
