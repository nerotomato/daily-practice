package com.nerotomato.ioc.config;

import com.nerotomato.ioc.bean.Student;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 通过@Configuration注解，表明这个类是一个spring配置类
 * 通过@ComponentScan注解，取代xml配置文件中的<context:component-scan>标签
 * 实现全部通过注解开发
 * Created by nero on 2021/4/19.
 */
@Configuration
@ComponentScan(basePackages = "com.nerotomato.ioc.bean")
public class SpringIocConfig {

    @Bean(initMethod = "init")
    public Student student() {
        return new Student();
    }
}
