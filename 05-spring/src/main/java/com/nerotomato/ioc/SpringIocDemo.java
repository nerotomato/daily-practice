package com.nerotomato.ioc;

import com.nerotomato.ioc.bean.Student;
import com.nerotomato.ioc.config.SpringIocConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 1.通过xmL方式，获取applicationContext.xml配置文件中配置的bean
 * 2.xml中开启component-scan组件扫描，并配合@Component注解装配bean
 * <context:component-scan base-package="com.nerotomato.ioc.bean"/>
 * 3.通过Java config配置，实现全注解方式加载bean
 * Created by nero on 2021/4/19.
 */
public class SpringIocDemo {

    public static void main(String[] args) {
        //1.xml方式
        //ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        //2.Java config + 注解方式 (纯注解)
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringIocConfig.class);
        Student student = (Student) context.getBean("student");
        System.out.println("   context.getBeanDefinitionNames() ===>> "
                + String.join(",", context.getBeanDefinitionNames()));
        System.out.println("Hello! My name is:" + student.getName() + ",and my age is:" + student.getAge());
    }
}
