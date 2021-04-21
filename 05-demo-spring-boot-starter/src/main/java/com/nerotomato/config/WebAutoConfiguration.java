package com.nerotomato.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 创建自动装配类
 * 并使用@Import注解导入WebConfig配置
 * Created by nero on 2021/4/20.
 */
@Configuration
@Import(WebConfig.class)
public class WebAutoConfiguration {
}
