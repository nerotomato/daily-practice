package com.nerotomato.rpcfx.demo.provider.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义bean工厂，简单实现spring ioc功能
 * 从springbeans.properties配置文件读取bean配置
 *
 * Created by nero on 2021/6/2.
 */
public class MyBeanFactory {

    private static Map<String, String> beanMap = new ConcurrentHashMap();

    public static Object getBean(String serviceClass) throws Exception {
        beanMap = getBeanMap();
        String beanName = null;
        Object bean = null;
        if (null != serviceClass && serviceClass.contains(".")) {
            beanName = serviceClass.substring(serviceClass.lastIndexOf(".") + 1);
        } else if (null != serviceClass) {
            beanName = serviceClass;
        } else {
            throw new Exception("Service:" + serviceClass + " can not be null!");
        }
        if (!beanMap.containsKey(beanName)) {
            throw new Exception("Service:" + serviceClass + " not found exception!");
        }
        try {
            Class<?> clazz = Class.forName(beanMap.get(beanName));
            bean = clazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return bean;
    }

    private static Map<String, String> getBeanMap() {
        Map<String, String> map = new ConcurrentHashMap<>();
        Properties props = new Properties();
        InputStream inputStream = MyBeanFactory.class.getClassLoader().getResourceAsStream("springbeans.properties");
        try {
            props.load(inputStream);
            Enumeration<?> enums = props.propertyNames();
            while (enums.hasMoreElements()) {
                String beanName = (String) enums.nextElement();
                System.out.println(beanName);
                map.put(beanName, props.getProperty(beanName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
