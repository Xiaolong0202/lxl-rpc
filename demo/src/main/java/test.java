import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import netty.AppClient;
import netty.AppServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class test {

    public static void main(String[] args) throws Exception {
//        new Thread(()->{
//            try {
//                new AppServer(8080).start();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }).start();
//        new Thread(()->{
//            try {
//                new AppClient().run();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
////        }).start();
//        byte[] bytes = new byte[]{11,23,23,23,23,23,23,23,23,23,23,12,21};
//        //使用GZIP进行压缩
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out);
//        gzipOutputStream.write(bytes);
//        gzipOutputStream.finish();
//        System.out.println(Arrays.toString(out.toByteArray()));
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[]{31, -117, 8, 0, 0, 0, 0, 0, 0, -1, -29, 22, -121, 3, 30, 81, -54, 17, 0, -34, 80, -31, -119, 79, 0, 0, 0});
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
        System.out.println(Arrays.toString(gzipInputStream.readAllBytes()));
    }
}
