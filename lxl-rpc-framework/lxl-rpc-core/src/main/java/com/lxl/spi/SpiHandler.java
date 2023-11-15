package com.lxl.spi;

import com.lxl.exceptions.SpiException;
import com.lxl.factory.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/24  23:53
 **/
@Slf4j
public class SpiHandler {

    public static final String BASE_PATH = "META-INF/lxl-rpc-services/";

    public static final String SEPARATOR = "-";

    //定义一个缓存，保存与SPI相关的原始内容
    private static final Map<String, List<String>> SPI_CONTENT = new HashMap<>(16);
    //缓存用存储 接口与实现类
    private static final Map<Class<?>, List<ObjectMapper<?>>> SPI_IMPLEMENT = new HashMap<>(16);

    //初始化静态变量 SPI_CONTENT  读取指定文件中的内容并加载至 内存中
    static {
        //首先加载jar 包和 工程中的classPath资源
        URL file_url = ClassLoader.getSystemClassLoader().getResource(BASE_PATH);
        if (file_url != null) {
            File file = new File(file_url.getPath());
            File[] files = file.listFiles();
            //遍历目录中的所有文件并读取至内存当中
            if (files != null) {
                for (File child : files) {
                    String key = child.getName();
                    List<String> value = new ArrayList<>();
                    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(child))) {
                        while (true) {
                            String line = bufferedReader.readLine();
                            if (line == null || "".equals(line.trim())) break;
                            else value.add(line);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    SPI_CONTENT.put(key, value);
                }
            }
        }
    }

    /**
     * 获取服务的实例
     * @param clazz
     * @return
     * @param <T>
     */
    public synchronized static <T> List<ObjectMapper<T>> getList(Class<T> clazz) {
        List<ObjectMapper<?>> objectList = SPI_IMPLEMENT.get(clazz);
        if (objectList !=null && objectList.size() > 0){
        return objectList.stream().map(objectMapper -> {
              return   (ObjectMapper<T>)objectMapper;
            }).toList();
        }


        buildCache(clazz);


        objectList = SPI_IMPLEMENT.get(clazz);
        if (objectList !=null && objectList.size() > 0){
            return objectList.stream().map(objectMapper -> {
                return   (ObjectMapper<T>)objectMapper;
            }).toList();
        }
        return null;
    }

    /**
     * 构建 Map<Class<?>, List<Object>> SPI_IMPLEMENT  的缓存
     * @param clazz
     */
    public static void buildCache(Class<?> clazz) {
        //建立缓存
        String clazzName = clazz.getName();
        List<String> implementList = SPI_CONTENT.get(clazzName);
        if (implementList == null) return;

        List<ObjectMapper<?>> instanceList = new ArrayList<>();
        //实例化所有的实现并进行缓存
        for (String implementName : implementList) {
            Class<?> implementClazz = null;
            try {
                String[] split = implementName.split(SEPARATOR);
                if (split.length!=3)throw new SpiException("您创建的SPI文件不合法");
                implementClazz = Class.forName(split[2]);
                byte code = Byte.parseByte(split[0]);
                String name = split[1];
                Object instance = implementClazz.getConstructor().newInstance();
                instanceList.add(new ObjectMapper<>(code, name, instance));
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                log.error("实例化【{}】的时候出现了异常", implementName, e);
            }
            SPI_IMPLEMENT.put(clazz, instanceList);
        }
    }
}
