1.创建数据库

分库：tomato_shop_0、tomato_shop_1

```sql
drop database if exists tomato_shop_0;
drop database if exists tomato_shop_1;
create database IF NOT EXISTS tomato_shop_0 default charset utf8mb4;
create database IF NOT EXISTS tomato_shop_1 default charset utf8mb4;
```

2.创建表

分表：每个库分别创建表 oms_order_0  到 oms_order_15，共16张表

```sql
DROP TABLE IF EXISTS oms_order_0;

CREATE TABLE `oms_order_0` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint NOT NULL COMMENT '用户id',
  `source_type` tinyint NOT NULL COMMENT '订单来源：1->APP;2->网页',
  `order_id` bigint DEFAULT NULL COMMENT '订单编号',  
  `member_username` varchar(64) NOT NULL COMMENT '用户帐号',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '订单总金额',
  `pay_type` tinyint DEFAULT NULL COMMENT '支付方式：1->支付宝；2->微信; 3->其他',  
  `status` tinyint DEFAULT NULL COMMENT '订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单',
  `order_type` tinyint DEFAULT NULL COMMENT '订单类型：0->正常订单；1->秒杀订单',  
  `receiver_name` varchar(100) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(32) NOT NULL COMMENT '收货人电话',
  `receiver_post_code` varchar(32) DEFAULT NULL COMMENT '收货人邮编',
  `receiver_province` varchar(32) DEFAULT NULL COMMENT '省份/直辖市',
  `receiver_city` varchar(32) DEFAULT NULL COMMENT '城市',
  `receiver_region` varchar(32) DEFAULT NULL COMMENT '区',
  `receiver_detail_address` varchar(200) DEFAULT NULL COMMENT '详细地址',
  `note` varchar(500) DEFAULT NULL COMMENT '订单备注',
  `confirm_status` tinyint DEFAULT NULL COMMENT '确认收货状态：0->未确认；1->已确认',
  `delete_status` tinyint NOT NULL DEFAULT '0' COMMENT '删除状态：0->未删除；1->已删除',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `comment_time` datetime DEFAULT NULL COMMENT '评价时间',
  `create_time` datetime DEFAULT NULL COMMENT '提交时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8 COMMENT='订单表';

#添加唯一索引
ALTER TABLE oms_order_0 ADD unique idx_order_id (order_id);
```

3.shardingsphere-proxy 配置

server.yaml

```yaml
authentication:
  users:
    root:
      password: 123456
    sharding:
      password: 123456 
      authorizedSchemas: tomato_shop_proxy

props:
  max-connections-size-per-query: 1
  acceptor-size: 16  # The default value is available processors count * 2. CPU核心数*2
  executor-size: 8   # Infinite by default. CPU核心数
  proxy-frontend-flush-threshold: 128  # The default value is 128.
    # LOCAL: Proxy will run with LOCAL transaction.
    # XA: Proxy will run with XA transaction.
    # BASE: Proxy will run with B.A.S.E transaction.
  proxy-transaction-type: LOCAL
  proxy-opentracing-enabled: false
  proxy-hint-enabled: false
  query-with-cipher-column: true
  sql-show: true
  check-table-metadata-enabled: false
```

config-sharding.yaml

```yaml
schemaName: tomato_shop_proxy

dataSources:
   ds0:
     url: jdbc:mysql://172.18.0.3:3306/tomato_shop_0?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
     username: root
     password: 123456
     connectionTimeoutMilliseconds: 30000
     idleTimeoutMilliseconds: 60000
     maxLifetimeMilliseconds: 1800000
     maxPoolSize: 50
     minPoolSize: 1
     maintenanceIntervalMilliseconds: 30000
   ds1:
     url: jdbc:mysql://172.18.0.3:3306/tomato_shop_1?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
     username: root
     password: 123456
     connectionTimeoutMilliseconds: 30000
     idleTimeoutMilliseconds: schemaName: tomato_shop_proxy

dataSources:
   ds0:
     url: jdbc:mysql://172.18.0.3:3306/tomato_shop_0?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
     username: root
     password: 123456
     connectionTimeoutMilliseconds: 30000
     idleTimeoutMilliseconds: 60000
     maxLifetimeMilliseconds: 1800000
     maxPoolSize: 50
     minPoolSize: 1
     maintenanceIntervalMilliseconds: 30000
   ds1:
     url: jdbc:mysql://172.18.0.3:3306/tomato_shop_1?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
     username: root
     password: 123456
     connectionTimeoutMilliseconds: 30000
     idleTimeoutMilliseconds: 60000
     maxLifetimeMilliseconds: 1800000
     maxPoolSize: 50
     minPoolSize: 1
     maintenanceIntervalMilliseconds: 30000
   ds0_slave0:
     url: jdbc:mysql://172.18.0.4:3306/tomato_shop_0?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
     username: root
     password: 123456
     connectionTimeoutMilliseconds: 30000
     idleTimeoutMilliseconds: 60000
     maxLifetimeMilliseconds: 1800000
     maxPoolSize: 50
     minPoolSize: 1
     maintenanceIntervalMilliseconds: 30000
   ds1_slave0:
     url: jdbc:mysql://172.18.0.4:3306/tomato_shop_1?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
     username: root
     password: 123456
     connectionTimeoutMilliseconds: 30000
     idleTimeoutMilliseconds: 60000
     maxLifetimeMilliseconds: 1800000
     maxPoolSize: 50
     minPoolSize: 1
     maintenanceIntervalMilliseconds: 30000
   
rules:
# 配置分片规则
- !SHARDING
    tables:
         # 配置表规则
        #逻辑订单表名称
        t_order:
          actualDataNodes: ds${0..1}.oms_order_${0..15}
          # 配置分库策略
          databaseStrategy:
            standard:
              shardingColumn: member_id
              shardingAlgorithmName: order_database_inline
          # 配置分表策略
          tableStrategy:
            standard:
              shardingColumn: order_id
              shardingAlgorithmName: order_table_inline
          # 配置key的生成算法
          keyGenerateStrategy:
            column: order_id
            keyGeneratorName: snowflake
        
        #逻辑用户表名称
        t_user:
          actualDataNodes: ds${0..1}.ums_member_${0..1}
          # 配置分库策略
          databaseStrategy:
            standard:
              shardingColumn: id
              shardingAlgorithmName: member_database_inline
          # 配置分表策略
          tableStrategy:
            standard:
              shardingColumn: id
              shardingAlgorithmName: member_table_inline
          # 配置key的生成算法
          keyGenerateStrategy:
            column: id
            keyGeneratorName: snowflake
    # defaultDatabaseStrategy:
        # standard:
              # shardingColumn: id
              # shardingAlgorithmName: database_inline
    # defaultTablesStrategy:
        # standard:
              # shardingColumn: id
              # shardingAlgorithmName: table_inline
        
    # 配置分片算法
    shardingAlgorithms:
        #用户表分库规则
        member_database_inline:
          type: INLINE
          props:
            algorithm-expression: ds${id % 2}
        #订单表分库规则
        order_database_inline:
          type: INLINE
          props:
            algorithm-expression: ds${member_id % 2}
        #用户表分表规则
        member_table_inline:
          type: INLINE
          props:
            algorithm-expression: ums_member_${id % 2}
        #订单表分表规则
        order_table_inline:
          type: INLINE
          props:
            algorithm-expression: oms_order_${order_id % 16}
    # 分布式序列算法配置
    keyGenerators:
        snowflake:
            type: SNOWFLAKE
            props:
                worker-id: 001
                
# - !READWRITE_SPLITTING
  # dataSources:
    # ms_ds0: # 读写分离逻辑数据源名称
      # # 写库数据源名称
      # writeDataSourceName: ds0 
      # # 读库数据源名称
      # readDataSourceNames: 
        # - ds0_slave0
      # # 负载均衡算法名称
      # #loadBalancerName: loadBalance-name 
    # ms_ds1: # 读写分离逻辑数据源名称
      # # 写库数据源名称
      # writeDataSourceName: ds1 
      # # 读库数据源名称
      # readDataSourceNames: 
        # - ds1_slave0
      # 负载均衡算法名称
      #loadBalancerName: loadBalance-name 
  
  # # 负载均衡算法配置
  # loadBalancers:
    # # 负载均衡算法名称
    # loadBalance-name:
      # # 负载均衡算法类型
      # type: ROUND_ROBIN
      # props: # 负载均衡算法属性配置
        # # ...
        
- !REPLICA_QUERY
  dataSources:
    ds0:
      primaryDataSourceName: ds0
      replicaDataSourceNames:
        - ds0_slave0
    # # 负载均衡算法名称
    # loadBalancerName: loadBalance-name 
    ds1:
      primaryDataSourceName: ds1
      replicaDataSourceNames:
        - ds1_slave0
    # # 负载均衡算法名称
    # loadBalancerName: loadBalance-name 
        
  # # 负载均衡算法配置
  # loadBalancers:
    # # 负载均衡算法名称
    # loadBalance-name:
      # # 负载均衡算法类型
      # type: ROUND_ROBIN
      # props: # 负载均衡算法属性配置
        # # ...
```

