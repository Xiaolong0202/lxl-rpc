package com.lxl.config.resolver;

import com.lxl.compress.Compressor;
import com.lxl.config.Configuration;
import com.lxl.core.IdGenerator;
import com.lxl.discovery.RegistryConfig;
import com.lxl.enumnation.CompressType;
import com.lxl.enumnation.SerializeType;
import com.lxl.loadbalance.LoadBalancer;
import com.lxl.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * @Author LiuXiaolong
 * @Description lxl-rpc
 * @DateTime 2023/8/24  19:26
 **/
@Slf4j
public class XMLResolver {


    /**
     * 从配置文件读取信息
     *
     * @param configuration
     */
    public void loadFromXml(Configuration configuration) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);//禁用DTD校验
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("LxlRpcConfig.xml");
            if (inputStream == null) return;
            //禁止校验DTD
            documentBuilder.setEntityResolver((publicId, systemId) -> new InputSource(new StringBufferInputStream("")));
            Document document = documentBuilder.parse(inputStream);
            //构建Xpath
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            //todo set属性之前先需要判断resolve的结果是否为NULL
            //解析所有的属性
            Integer port = resolvePort(document, xPath);
            if (port != null) configuration.setPORT(port);

            String appName = resolveAppName(document, xPath);
            if (appName != null) configuration.setAppName(appName);

            IdGenerator idGenerator = resolveIdGenerator(document, xPath);
            if (idGenerator != null) configuration.setIdGenerator(idGenerator);

            RegistryConfig registryConfig = resolveRegistryConfig(document, xPath);
            if (registryConfig != null) configuration.setRegistryConfig(registryConfig);

            String serializeType = resolveSerializeType(document, xPath);
            if (serializeType !=null)configuration.setSerializeType(serializeType);

            Serializer serializer = resolveSerializer(document, xPath);
            if (serializer != null) configuration.setSerializer(serializer);


            String compressType = resolveCompressType(document, xPath);
            if (compressType!=null)configuration.setCompressType(compressType);

            Compressor compressor = resolveCompress(document, xPath);
            if (compressor != null) configuration.setCompressor(compressor);

            LoadBalancer loadBalancer = resolveLoadBalancer(document, xPath);
            if (loadBalancer != null) configuration.setLoadBalancer(loadBalancer);

            log.info("从xml当中读取配置成功");
            //如果还有其他的新的标签则继续添加新的方法
        } catch (SAXException | IOException | ParserConfigurationException e) {
            log.info("为发现对应的配置文件或者,解析XML文件的时候出现了异常,将选用默认的配置");
//            throw new RuntimeException(e);
        }
    }

    /**
     * 解析序列化器
     *
     * @param document
     * @param xPath
     * @return
     */
    private Serializer resolveSerializer(Document document, XPath xPath) {
        String expression = "/configuration/serializer[1]";
        return parseObject(document, xPath, expression, null);
    }

    /**
     * 解析解/压缩器
     *
     * @param document
     * @param xPath
     * @return
     */
    private Compressor resolveCompress(Document document, XPath xPath) {
        String expression = "/configuration/compressor[1]";
        return parseObject(document, xPath, expression, null);
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
    private String resolveCompressType(Document document, XPath xPath) {
        String expression = "/configuration/compressType[1]";
        String type = parseAttribute(document, xPath, expression, "type");
        if (type == null) return null;
        return type.toUpperCase();
    }

    /**
     * 解析序列化的类型
     *
     * @param document
     * @param xPath
     * @return
     */
    private String resolveSerializeType(Document document, XPath xPath) {
        String expression = "/configuration/serializeType[1]";
        String type = parseAttribute(document, xPath, expression, "type");
        if (type == null) return null;
        return type.toUpperCase();
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
        if (connextString == null) return null;
        return new RegistryConfig(connextString);
    }

    /**
     * 解析ID发号器
     *
     * @param document
     * @param xPath
     * @return
     */
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
    private Integer resolvePort(Document document, XPath xPath) {
        String expression = "/configuration/port[1]";
        String port = parseNodeContent(document, xPath, expression);
        if (port == null) return null;
        return Integer.parseInt(port);
    }

    /**
     * 解析一个结点返回一个对象实例
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
            if (classNode == null) {
                log.debug("【{}】节点在XML配置文件当中不存在", expression);
                return null;
            }
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

    /**
     * 解析某个节点的属性
     *
     * @param document
     * @param xPath
     * @param expression
     * @param attrName
     * @return
     */
    public String parseAttribute(Document document, XPath xPath, String expression, String attrName) {
        try {
            //构建xpath表达式.XPathConstants.STRING指定的返回的类型
            Node node = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);
            if (node == null) {
                log.debug("【{}】节点在XML配置文件当中不存在", expression);
                return null;
            }
            Node attr = node.getAttributes().getNamedItem(attrName);
            return attr.getNodeValue();
        } catch (XPathExpressionException e) {
            log.error("解析表达式时发生了异常");
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析某个节点的内容
     *
     * @param document
     * @param xPath
     * @param expression
     * @return
     */
    public String parseNodeContent(Document document, XPath xPath, String expression) {
        try {
            //构建xpath表达式.XPathConstants.STRING指定的返回的类型
            Node node = (Node) xPath.evaluate(expression, document, XPathConstants.NODE);
            if (node == null) {
                log.debug("【{}】节点在XML配置文件当中不存在", expression);
                return null;
            }
            return node.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("解析表达式时发生了异常");
            throw new RuntimeException(e);
        }
    }

}
