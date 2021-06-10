package com.nerotomto.forex;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by nero on 2021/6/8.
 */
@SpringBootApplication
@ImportResource({"classpath:spring-dubbo-provider.xml"})
@MapperScan(value = "com.nerotomto.forex.mapper")
public class ForexAccountApplication {
    public static void main(String[] args) {
        /*SpringApplication springApplication = new SpringApplication(ForexAccountApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);*/
        SpringApplication.run(ForexAccountApplication.class, args);
    }
}
