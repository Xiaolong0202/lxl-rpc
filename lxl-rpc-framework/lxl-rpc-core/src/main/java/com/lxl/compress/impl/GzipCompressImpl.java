package com.lxl.compress.impl;

import com.lxl.compress.Compresser;
import com.lxl.exceptions.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/18  16:12
 **/
@Slf4j
public class GzipCompressImpl implements Compresser {
    @Override
    public byte[] compress(byte[] bytes) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out);) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] res = out.toByteArray();
            if (log.isDebugEnabled()) log.debug("完成字节流的压缩---【{}】","gzip");
            return res;
        } catch (IOException e) {
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);GZIPInputStream gzipInputStream = new GZIPInputStream(in) ){
            byte[] allBytes = gzipInputStream.readAllBytes();
            if (log.isDebugEnabled()) log.debug("完成字节流的解压缩---【{}】","gzip");
            return allBytes;
        } catch (IOException e) {
            throw new CompressException(e);
        }
    }

}
