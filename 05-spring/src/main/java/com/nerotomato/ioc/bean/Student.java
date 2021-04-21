package com.nerotomato.ioc.bean;

import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 通过@Component注解将Student类作为spring bean 加载到容器中
 * 实现InitializingBean接口，调用afterPropertiesSet()方法实现bean的初始化
 * <p>
 * Created by nero on 2021/4/19.
 */
@Data
//@Component
//public class Student implements InitializingBean {
public class Student {
    private int id;
    private int age;
    private String name;

    public void init() {
        if (age == 0) {
            age = 25;
        }
        if (name == null) {
            name = "nerotomato";
        }
        System.out.println("Student bean has been initialized.");
    }

    /**
     * 实现InitializingBean接口，调用afterPropertiesSet()方法实现bean的初始化
     */
    /*@Override
    public void afterPropertiesSet() throws Exception {
        init();
    }*/
}
