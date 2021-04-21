package com.nerotomato.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.nerotomato.service.IStudentService;
import com.nerotomato.service.StudentServiceImpl;

/**
 * Spring配置类
 * Created by nero on 2021/4/20.
 */
@Configuration
public class WebConfig {
    @Bean
    public IStudentService getService(){
        return new StudentServiceImpl();
    }
}
