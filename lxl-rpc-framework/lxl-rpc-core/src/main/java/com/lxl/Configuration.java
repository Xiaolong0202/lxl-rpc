package com.lxl;

import com.lxl.discovery.RegistryConfig;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.SerializeType;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.loadbalance.impl.RoundLoadBalancer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * 全局的配置类： 代码配置-->xml配置-->spi配置-->默认项
 *
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/23  1:18
 **/
@Data
@Slf4j
public class Configuration {
    //配置信息-->端口号
    private int PORT = 8094;

    //应用程序的名字
    private String appName = "default";

    private ProtocolConfig protocolConfig;
    //注册配置
    private RegistryConfig registryConfig;

    //配置信息--ID生成器
    private IdGenerator idGenerator = new IdGenerator(1L, 2L);
    //序列化的类型
    private SerializeType serializeType = SerializeType.JDK;
    //压缩的类型
    private CompressType compressType = CompressType.GZIP;
    //负载均衡策略
    private LoadBalancer loadBalancer = new RoundLoadBalancer();

    //读取xml


    //进行配置

    public Configuration() {
        loadFromXml(this);
    }

    /**
     * 从配置文件读取信息
     *
     * @param configuration
     */
    private void loadFromXml(Configuration configuration) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);//禁用DTD校验
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("LxlRpcConfig.xml");
            //禁止校验DTD
            documentBuilder.setEntityResolver((publicId, systemId) -> new InputSource(new StringBufferInputStream("")));
            Document document = documentBuilder.parse(inputStream);
            //构建Xpath
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            //解析所有的属性
            this.setPORT(resolvePort(document, xPath));
            this.setAppName(resolveAppName(document, xPath));
            this.setIdGenerator(resolveIdGenerator(document, xPath));
            this.setRegistryConfig(resolveRegistryConfig(document, xPath));
            this.setSerializeType(resolveSerializeType(document, xPath));
            this.setCompressType(resolveCompressType(document, xPath));
            this.setLoadBalancer(resolveLoadBalancer(document, xPath));
            log.info(configuration.toString());

        } catch (SAXException | IOException | ParserConfigurationException e) {
            log.info("为发现对应的配置文件或者,解析XML文件的时候出现了异常,将选用默认的配置");
            throw new RuntimeException(e);
        }
    }

    /**
     * 从XML中解析负载均衡类
     *
     * @param document
     * @param xPath
     * @return
     */
    private LoadBalancer resolveLoadBalancer(Document document, XPath xPath) {
        String expression = "/configuration/loadBalancer[1]";
        return parseObject(document, xPath, expression, null);
    }

    /**
     * 解析压缩的类型
     *
     * @param document
     * @param xPath
     * @return
     */
    private CompressType resolveCompressType(Document document, XPath xPath) {
        String expression = "/configuration/compressType[1]";
        String type = parseAttribute(document, xPath, expression, "type");
        return CompressType.valueOf(type.toUpperCase());
    }

    /**
     * 解析序列化的类型
     *
     * @param document
     * @param xPath
     * @return
     */
    private SerializeType resolveSerializeType(Document document, XPath xPath) {
        String expression = "/configuration/serializeType[1]";
        String type = parseAttribute(document, xPath, expression, "type");
        return SerializeType.valueOf(type.toUpperCase());
    }

    /**
     * 解析注册中心
     *
     * @param document
     * @param xPath
     * @return
     */
    private RegistryConfig resolveRegistryConfig(Document document, XPath xPath) {
        String expression = "/configuration/registry[1]";
        String connextString = parseAttribute(document, xPath, expression, "url");
        return new RegistryConfig(connextString);
    }

    private IdGenerator resolveIdGenerator(Document document, XPath xPath) {
        String expression = "/configuration/idGenerator[1]";
        String dataCenterId = parseAttribute(document, xPath, expression, "dataCenterId");
        String machineId = parseAttribute(document, xPath, expression, "machineId");
        return parseObject(document, xPath, expression, new Class[]{long.class, long.class}, Long.parseLong(dataCenterId), Long.parseLong(machineId));
    }

    /**
     * 从xml当中解析AppName
     *
     * @param document
     * @param xPath
     * @return
     */
    private String resolveAppName(Document document, XPath xPath) {
        String expression = "/configuration/appName[1]";
        return parseNodeContent(document, xPath, expression);
    }

    /**
     * 从xml中解析端口号
     *
     * @param document
     * @param xPath
     * @return
     */
    private int resolvePort(Document document, XPath xPath) {
        String expression = "/configuration/port[1]";
        String port = parseNodeContent(document, xPath, expression);
        return Integer.parseInt(port);
    }

    /**
     * 解析一个结点返回一个实例
     *
     * @param document
     * @param xPath
     * @param expression
     * @param paramType
     * @param params
     * @param <T>
     * @return
     * @throws XPathExpressionException
     */
    private <T> T parseObject(Document document, XPath xPath, String expression, Class<?>[] paramType, Object... params) {
        try {
            //构建xpath表达式.XPathConstants.STRING指定的返回的类型
            Node classNode = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);
            Node classNameAttr = classNode.getAttributes().getNamedItem("class");
            String className = classNameAttr.getNodeValue();
            Class<?> aClass = Class.forName(className);
            Object instance = null;
            if (paramType == null) {
                instance = aClass.getConstructor().newInstance();
            } else {
                instance = aClass.getConstructor(paramType).newInstance(params);
            }
            return (T) instance;
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException | XPathExpressionException e) {
            log.error("解析表达式时发生了异常");
            throw new RuntimeException(e);
        }
    }

    public String parseAttribute(Document document, XPath xPath, String expression, String attrName) {
        try {
            //构建xpath表达式.XPathConstants.STRING指定的返回的类型
            Node node = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);
            Node attr = node.getAttributes().getNamedItem(attrName);
            return attr.getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("解析表达式时发生了异常");
            throw new RuntimeException(e);
        }
    }

    public String parseNodeContent(Document document, XPath xPath, String expression) {
        try {
            //构建xpath表达式.XPathConstants.STRING指定的返回的类型
            Node node = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);
            return node.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("解析表达式时发生了异常");
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Configuration();
    }

}
