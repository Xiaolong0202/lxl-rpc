package com.lxl.compress.impl;

import com.lxl.compress.Compresser;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/18  16:12
 **/
@Slf4j
public class ZipCompresserImpl implements Compresser {

    @Override
    public byte[] compress(byte[] bytes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream gzipOutputStream = new ZipOutputStream(out);) {
            gzipOutputStream.write(bytes);
            byte[] res = out.toByteArray();
            if (log.isDebugEnabled()) log.debug("完成字节流的压缩---ZIP");
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes); ZipInputStream gzipInputStream = new ZipInputStream(in)){
            byte[] allBytes = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()) log.debug("完成字节流的解压缩---ZIP");
            return allBytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
