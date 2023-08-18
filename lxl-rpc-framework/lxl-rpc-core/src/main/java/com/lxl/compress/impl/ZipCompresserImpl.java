package com.lxl.compress.impl;

import com.lxl.compress.Compresser;
import com.lxl.exceptions.CompressException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.*;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/18  16:12
 **/
@Slf4j
public class ZipCompresserImpl implements Compresser {

    @Override
    public byte[] compress(byte[] bytes) {
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(bos);
            ZipEntry entry = new ZipEntry("zip");
            entry.setSize(bytes.length);
            zip.putNextEntry(entry);
            zip.write(bytes);
            zip.closeEntry();
            zip.close();
            b = bos.toByteArray();
            bos.close();
        } catch (Exception ex) {
            throw new CompressException(ex);
        }
        if (log.isDebugEnabled()) log.debug("完成字节流的压缩---【{}】","zip");
        return b;
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ZipInputStream zip = new ZipInputStream(bis);
            while (zip.getNextEntry() != null) {
                byte[] buf = new byte[1024];
                int num = -1;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((num = zip.read(buf, 0, buf.length)) != -1) {
                    baos.write(buf, 0, num);
                }
                b = baos.toByteArray();
                baos.flush();
                baos.close();
            }
            zip.close();
            bis.close();
        } catch (Exception ex) {
            throw new CompressException(ex);
        }
        if (log.isDebugEnabled()) log.debug("完成字节流的解压缩---【{}】","zip");
        return b;
    }
}
