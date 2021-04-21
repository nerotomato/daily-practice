package com.nerotomato.ioc;

import com.nerotomato.ioc.bean.Student;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

@ExtendWith(SpringExtension.class)//指示JUnit Jupiter（Junit5）使用Spring支持扩展测试。
@ContextConfiguration("classpath:applicationContext.xml")//指定要为此测试类加载的Spring配置文件
public class SpringIocDemoTest {

    //自动装配
    /*@Autowired
    private Student student;*/
    @Resource(name = "student")
    private Student student;

    /**
     * 1、进行单元测试的方法不能有返回值，否则会报 No test were found
     * 2、进行单元测试的方法不能私有化
     * 3、junit版本问题
     */
    @Test
    public void show() {
        System.out.println("Hello! My name is:" + student.getName() + ",and my age is:" + student.getAge());
    }
}