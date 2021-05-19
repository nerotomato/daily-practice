package com.nerotomato.config;

import com.nerotomato.datasource.router.MyAbstractRoutingDataSource;
import com.nerotomato.datasource.type.DynamicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源配置类
 * Created by nero on 2021/5/19.
 */
@Configuration
public class DataSourceConfig {

    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource getMasterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource getSlaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "dynamicDataSource")
    public MyAbstractRoutingDataSource dynamicDatasource(@Qualifier("masterDataSource") DataSource masterDataSource,
                                                         @Qualifier("slaveDataSource") DataSource slaveDataSource) {
        MyAbstractRoutingDataSource dataSourceRouter = new MyAbstractRoutingDataSource();

        dataSourceRouter.setDefaultTargetDataSource(masterDataSource);
        Map<Object, Object> targetDataSourceMap = new HashMap<>();
        targetDataSourceMap.put(DynamicDataSource.MASTER, masterDataSource);
        targetDataSourceMap.put(DynamicDataSource.SLAVE, slaveDataSource);
        dataSourceRouter.setTargetDataSources(targetDataSourceMap);
        return dataSourceRouter;
    }

}