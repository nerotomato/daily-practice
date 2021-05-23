package com.nerotomato.shardingspherejdbc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Created by nero on 2021/4/19.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
//@MapperScan("com.nerotomato.shardingspherejdbc.mapper")
public class SpringBootDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootDemoApplication.class, args);
    }
}



