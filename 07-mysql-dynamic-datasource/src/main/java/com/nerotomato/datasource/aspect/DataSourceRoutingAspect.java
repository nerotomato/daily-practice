package com.nerotomato.datasource.aspect;

import com.nerotomato.datasource.annotation.DataSourceRouting;
import com.nerotomato.datasource.context.DataSourceContextHolder;
import com.nerotomato.datasource.type.DynamicDataSource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 创建切面来处理注解 DataSourceRouting
 * Created by nero on 2021/5/19.
 */
@Slf4j
@Aspect
@Component
public class DataSourceRoutingAspect {

    @Around("@annotation(dataSourceRouting)")
    public Object aroundDataSourceRouting(ProceedingJoinPoint joinPoint,
                                          DataSourceRouting dataSourceRouting) throws Throwable {
        DynamicDataSource dynamicDataSource = DataSourceContextHolder.getDynamicDataSource();
        log.info("Setting dataSource {} into DataSourceContext", dataSourceRouting.value());
        DataSourceContextHolder.set(dataSourceRouting.value());

        try {
            return joinPoint.proceed();
        } finally {
            if (dynamicDataSource != null) {
                // revert context back to previous state after execute the method
                DataSourceContextHolder.set(dynamicDataSource);
            } else {
                // there is no value being set into the context before, just clear the context
                // to prevent memory leak
                DataSourceContextHolder.clear();
            }
        }

    }

}
