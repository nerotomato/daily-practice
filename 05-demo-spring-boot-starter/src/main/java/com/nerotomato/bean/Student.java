package com.nerotomato.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * 通过@Component注解将Student类作为spring bean 加载到容器中
 * 实现InitializingBean接口，调用afterPropertiesSet()方法实现bean的初始化
 * Created by nero on 2021/4/19.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private int id;
    private int age;
    private String name;

    {
        //构造代码块若被执行，说明该类被实例化了，即被springboot自动装配了
        System.out.println("========>This is student!");
    }

    public void init() {
        if (age == 0) {
            age = 25;
        }
        if (name == null) {
            name = "nerotomato";
        }
        System.out.println("Student bean has been initialized.");
    }

}
