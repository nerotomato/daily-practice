package com.nerotomato.shardingspherejdbc.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * 多数据源配置类
 * Created by nero on 2021/5/19.
 */
@Configuration
public class DataSourceConfig {

    @Bean(name = "masterDataSource")
    @ConfigurationProperties(prefix = "spring.shardingsphere.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "slaveDataSource")
    @ConfigurationProperties(prefix = "spring.shardingsphere.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public DataSource dataSource(@Qualifier("masterDataSource") DataSource masterDataSource,
                                 @Qualifier("slaveDataSource") DataSource slaveDataSource) {
        // 配置真实数据源
        Map<String, DataSource> dataSourceMap = new HashMap<>();

        // 配置第 1 个数据源：主库负责写
        dataSourceMap.put("ds0_master0", masterDataSource);

        // 配置第 2 个数据源:从库负责读
        dataSourceMap.put("ds0_replica0", slaveDataSource);

        // 配置 t_order 表规则
        //ShardingTableRuleConfiguration orderTableRuleConfig = new ShardingTableRuleConfiguration("t_test", "ds${0..1}.t_test_${0..1}");

        // 配置分库策略
        //orderTableRuleConfig.setDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("column_id", "dbShardingAlgorithm"));

        // 配置分表策略
        //orderTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("title_id", "tableShardingAlgorithm"));

        // 省略配置 t_order_item 表规则...
        // ...

        // 配置分片规则
        /*ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(orderTableRuleConfig);*/
        //读写分离配置 从库负载均衡算法配置:"roundRobin"
        ReplicaQueryDataSourceRuleConfiguration replicaQueryConfiguration =
                new ReplicaQueryDataSourceRuleConfiguration("ds0", "ds0_master0", Arrays.asList("ds0_replica0"), "roundRobin");

        //负载均衡算法
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalanceMaps = new HashMap<>
                (1);
        loadBalanceMaps.put("roundRobin", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()));
        //读写分离规则
        ReplicaQueryRuleConfiguration replicaQueryRuleConfig = new
                ReplicaQueryRuleConfiguration(Arrays.asList(replicaQueryConfiguration), loadBalanceMaps);
        // 配置分库算法
        /*Properties dbShardingAlgorithmrProps = new Properties();
        dbShardingAlgorithmrProps.setProperty("algorithm-expression", "ds${column_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("dbShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE", dbShardingAlgorithmrProps));*/

        // 配置分表算法
        /*Properties tableShardingAlgorithmrProps = new Properties();
        tableShardingAlgorithmrProps.setProperty("algorithm-expression", "t_test_${title_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("tableShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE", tableShardingAlgorithmrProps));*/
        Properties otherProperties = new Properties();
        otherProperties.setProperty("sql-show", "true");
        otherProperties.setProperty("query-with-cipher-column", "true");
        DataSource dataSource = null;
        try {
            dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap,
                    Arrays.asList(replicaQueryRuleConfig), otherProperties);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //logger.info("datasource : {}", dataSource);
        return dataSource;
    }
}
