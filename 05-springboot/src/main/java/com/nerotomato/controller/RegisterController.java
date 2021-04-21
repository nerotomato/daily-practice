package com.nerotomato.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.nerotomato.service.IStudentService;

/**
 * Created by nero on 2021/4/20.
 */
@Controller
@RequestMapping("/student")
public class RegisterController {
    @Autowired
    IStudentService service;

    @RequestMapping("/register")
    @ResponseBody
    public String register(@RequestParam int age, @RequestParam String name) {
        return service.registerStudent(age, name);
    }
}
