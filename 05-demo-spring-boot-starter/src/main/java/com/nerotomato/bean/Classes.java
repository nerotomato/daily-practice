package com.nerotomato.bean;

import lombok.Data;

import java.util.List;

/**
 * Created by nero on 2021/4/19.
 */
@Data
public class Classes {
    private int id;
    private String className;
    private List<Student> studentList;

    {
        //构造代码块若被执行，说明该类被实例化了，即被springboot自动装配了
        System.out.println("========>This is Class!");
    }
}
