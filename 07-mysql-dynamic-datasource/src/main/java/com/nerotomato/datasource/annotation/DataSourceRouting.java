package com.nerotomato.datasource.annotation;


import com.nerotomato.datasource.type.DynamicDataSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解
 * 通过注解 DataSourceRoute 来标识走master/slave
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSourceRouting {
    DynamicDataSource value() default DynamicDataSource.MASTER;
}
