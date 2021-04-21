package com.nerotomato.service;

import com.nerotomato.bean.School;
import com.nerotomato.bean.Student;
import com.nerotomato.bean.Classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nero on 2021/4/20.
 */
public class StudentServiceImpl implements IStudentService {
    {
        //构造代码块若被执行，说明该类被实例化了，即被springboot自动装配了
        System.out.println("========>This is IStudentService!");
    }

    @Override
    public String registerStudent(int age, String name) {
        Student student = new Student(1, age, name);
        Classes classes = new Classes();
        List<Student> studentList = new ArrayList<>();
        studentList.add(student);
        classes.setStudentList(studentList);

        School school = new School();
        List<Classes> classList = new ArrayList<>();
        classList.add(classes);
        school.setClassList(classList);

        return school.toString();
    }
}
