package com.nerotomato;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Created by nero on 2021/4/19.
 */
//将APP标记为@EnableAutoConfiguration
@EnableAutoConfiguration
public class APP {
    public static void main(String[] args) {
        SpringApplication.run(APP.class, args);
    }
}



