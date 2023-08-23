package com.lxl;

import com.lxl.discovery.RegistryConfig;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.SerializeType;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.loadbalance.impl.RoundLoadBalancer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * 全局的配置类： 代码配置-->xml配置-->spi配置-->默认项
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/23  1:18
 **/
@Data
@Slf4j
public class Configuration {
        //配置信息-->端口号
        private    int PORT = 8094;

        //应用程序的名字
        private String appName = "default";

        private ProtocolConfig protocolConfig;
        //注册配置
        private RegistryConfig registryConfig;

        //配置信息--ID生成器
        private   IdGenerator idGenerator = new IdGenerator(1,2);
        //序列化的类型
        private   SerializeType serializeType = SerializeType.JDK;
        //压缩的类型
        private   CompressType compressType = CompressType.GZIP;
        //负载均衡策略
        private LoadBalancer loadBalancer = new RoundLoadBalancer();

        //读取xml


        //进行配置

        public Configuration(){
                loadFromXml(this);
        }

        /**
         * 从配置文件读取信息
         * @param configuration
         */
        private void loadFromXml(Configuration configuration) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = null;
                try {
                        documentBuilder = documentBuilderFactory.newDocumentBuilder();
                        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("LxlRpcConfig.xml");
                        Document document = documentBuilder.parse(inputStream);


                        XPathFactory xPathFactory = XPathFactory.newInstance();
                        XPath xPath = xPathFactory.newXPath();
                        String expression = "/configuration/serializeType[1]";
                        String s = pareseString(document, xPath, expression);
                        SerializeType serializeType1 = SerializeType.valueOf(s);
                        System.out.println("serializeType1 = " + serializeType1);
                        System.out.println("s = " + s);
                } catch (SAXException | IOException | ParserConfigurationException e) {
                        log.info("为发现对应的配置文件或者,解析XML文件的时候出现了异常,将选用默认的配置");
                }
        }

        /**
         * 解析一个结点返回一个实例
         * @param document
         * @param xPath
         * @param expression
         * @param paramType
         * @param params
         * @return
         * @param <T>
         * @throws XPathExpressionException
         */
        private  <T> T parseObject(Document document, XPath xPath ,String expression,Class<?>[] paramType,Object... params) throws XPathExpressionException {
               try {
                       //构建xpath表达式.XPathConstants.STRING指定的返回的类型
                       Node classNode = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);
                       Node classNameAttr = classNode.getAttributes().getNamedItem("class");
                       String className = classNameAttr.getNodeValue();
                       Class<?> aClass = Class.forName(className);
                       Object instance = null;
                       if (paramType == null){
                               instance  =  aClass.getConstructor().newInstance();
                       }else {
                               instance = aClass.getConstructor(paramType).newInstance(params);
                       }
                       return (T) instance;
               } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                        IllegalAccessException | NoSuchMethodException e) {
                       log.error("解析表达式时发生了异常");
                       throw new RuntimeException(e);
               }
        }

        public String pareseString(Document document, XPath xPath ,String expression){
                try {
                        //构建xpath表达式.XPathConstants.STRING指定的返回的类型
                        Node classNode = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);
                        Node classNameAttr = classNode.getAttributes().getNamedItem("type");
                        return classNameAttr.getNodeValue();
                } catch (XPathExpressionException e) {
                        log.error("解析表达式时发生了异常");
                        throw new RuntimeException(e);
                }
        }
        public static void main(String[] args) {
                new Configuration();
        }

}
