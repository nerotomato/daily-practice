# 1.创建用户账号金额数据库

**分库：**

```sql
drop database if exists money_0;
drop database if exists money_1;
create database IF NOT EXISTS money_0 default charset utf8mb4;
create database IF NOT EXISTS money_1 default charset utf8mb4;
```

# 2.创建用户金额表

**分表：**

```sql
DROP TABLE IF EXISTS forex_account_0;

CREATE TABLE `forex_account_0` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `cny_wallet` decimal(13,2) COMMENT '用户人民币账号余额',
  `us_wallet` decimal(13,2) COMMENT '用户美元账号余额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='用户外汇账号金额表';

DROP TABLE IF EXISTS forex_account_1;

CREATE TABLE `forex_account_1` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `cny_wallet` decimal(13,2) COMMENT '用户人民币账号余额',
  `us_wallet` decimal(13,2) COMMENT '用户美元账号余额',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='用户外汇账号金额表';
```



# 3.通用API模块：

**forex-account-service-api**

用于服务提供者和消费者之间共享的数据

**外汇金额账户信息实体类：ForexAccount**

```java
package forex.account.api.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户外汇账号金额表
 * </p>
 *
 * @author nero
 * @since 2021-06-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ForexAccount对象", description = "用户外汇账号金额表")
public class ForexAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户人民币账号余额")
    private BigDecimal cnyWallet;

    @ApiModelProperty(value = "用户美元账号余额")
    private BigDecimal usWallet;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
```

**用于注册的服务Service: ForexAccountService**

```java
package forex.account.api.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import forex.account.api.entity.ForexAccount;

/**
 * <p>
 * 用户外汇账号金额表 服务类
 * </p>
 *
 * @author nero
 * @since 2021-06-08
 */
public interface ForexAccountService {

    Object exchangeMoney(ForexAccount forexAccount);

    Object save(ForexAccount forexAccount);

    Object remove(QueryWrapper<ForexAccount> queryWrapper);
}
```

# 4.服务提供方Provider

**forex-account-service**

**服务提供方dubbo配置spring-dubbo-provider.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://code.alibabatech.com/schema/dubbo
       http://code.alibabatech.com/schema/dubbo/dubbo.xsd">

    <dubbo:application name="forex-account-service-provider"/>

    <dubbo:registry protocol="zookeeper" address="127.0.0.1:2181"/>

    <dubbo:protocol name="dubbo" port="20880"
                    server="netty" client="netty"
                    charset="UTF-8" threadpool="fixed" threads="500"
                    queues="0" buffer="8192" accepts="0" payload="8388608"/>

    <!--    这里注册账号金额操作服务，ref="forexAccountService",需要和ForexAccountServiceImpl的名称对应-->
    <dubbo:service interface="forex.account.api.service.ForexAccountService"
                   ref="forexAccountService" executes="20"/>

</beans>
```

**服务提供方hmily配置hmily.yml**

```yaml
hmily:
  server:
    configMode: local
    appName: forex account service
  #  如果server.configMode eq local 的时候才会读取到这里的配置信息.
  config:
    appName: forex account service
    serializer: kryo
    contextTransmittalMode: threadLocal
    scheduledThreadMax: 16
    scheduledRecoveryDelay: 60
    scheduledCleanDelay: 60
    scheduledPhyDeletedDelay: 600
    scheduledInitDelay: 30
    recoverDelayTime: 60
    cleanDelayTime: 180
    limit: 200
    retryMax: 10
    bufferSize: 8192
    consumerThreads: 16
    asyncRepository: true
    autoSql: true
    phyDeleted: true
    storeDays: 3
    repository: mysql

repository:
  database:
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/hmily?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    maxActive: 20
    minIdle: 10
    connectionTimeout: 30000
    idleTimeout: 600000
    maxLifetime: 1800000
  file:
    path:
    prefix: /hmily
  mongo:
    databaseName:
    url:
    userName:
    password:
  zookeeper:
    host: localhost:2181
    sessionTimeOut: 1000
    rootPath: /hmily
  redis:
    cluster: false
    sentinel: false
    clusterUrl:
    sentinelUrl:
    masterName:
    hostName:
    port:
    password:
    maxTotal: 8
    maxIdle: 8
    minIdle: 2
    maxWaitMillis: -1
    minEvictableIdleTimeMillis: 1800000
    softMinEvictableIdleTimeMillis: 1800000
    numTestsPerEvictionRun: 3
    testOnCreate: false
    testOnBorrow: false
    testOnReturn: false
    testWhileIdle: false
    timeBetweenEvictionRunsMillis: -1
    blockWhenExhausted: true
    timeOut: 1000

metrics:
  metricsName: prometheus
  host:
  port: 9091
  async: true
  threadCount: 16
  jmxConfig:
```



**服务提供方application.yml配置，这里使用了shardingsphere-jdbc进行了分库分表,分库分表策略可根据实际情况配置，这里只是样例，实际上还是有些不合理的地方**

```yaml
logging:
  level:
    root: info

#mybatis-plus映射mapper文件
mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

spring:
  #配置分库分表策略
  shardingsphere:
    datasource:
      common:
        driver-class-name: com.mysql.cj.jdbc.Driver
        type: com.zaxxer.hikari.HikariDataSource
      ds0:
        autoCommit: false
        jdbc-url: jdbc:mysql://localhost:3306/money_0?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        password: 123456
        username: root
      ds1:
        autoCommit: false
        jdbc-url: jdbc:mysql://localhost:3306/money_1?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        password: 123456
        username: root
      names: ds0,ds1
    rules:
      props:
        sql:
          show: true
      sharding:
        key-generators:
          snowflake:
            props:
              worker-id: 33
            type: snowflake
        sharding-algorithms:
          database-inline:
            props:
              algorithm-expression: ds$->{id % 2}
            type: INLINE
          table-inline:
            props:
              algorithm-expression: forex_account_$->{id % 2}
            type: INLINE
        tables:
          #由于整合了mybatis-plus，所以这里逻辑表名称改成跟实际表名称一致
          forex_account:
            actual-data-nodes: ds$->{0..1}.forex_account_$->{0..1}
            database-strategy:
              standard:
                sharding-algorithm-name: database-inline
                sharding-column: id
            key-generate-strategy:
              column: id
              key-generator-name: snowflake
            key-generator:
              column: id
              props:
                worker:
                  id: 33
              type: SNOWFLAKE
            table-strategy:
              standard:
                sharding-algorithm-name: table-inline
                sharding-column: id
swagger:
  application-description: Dubbo hmily tcc
  application-name: Forex Exchange Service
  application-version: 1.0
  enable: true
  try-host: http://localhost:${server.port}
```

**具体服务实现类：**

**在需要开启TCC事务的方法exchangeMoney上使用@HmilyTCC注解：**

```java
@HmilyTCC(confirmMethod = "confirmExchange", cancelMethod = "cancelExchange")
```

**注意：注解中声明的confirmMethod和cancelMethod方法的参数列表与返回类型应与使用@HmilyTCC注解标识的方法一致。**

**如果不一致，会报错，报No such method exception，一开始以为是方法名称没对上，但是仔细检查了好几遍，名称就是一致的。这个问题困扰了很久，最后在hmily官方文档上看到才发现的是因为参数类型不一致导致的。**



```java
package com.nerotomto.forex.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.nerotomto.forex.mapper.ForexAccountMapper;
import forex.account.api.entity.ForexAccount;
import forex.account.api.service.ForexAccountService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 用户外汇账号金额表 服务实现类
 * </p>
 *
 * @author nero
 * @since 2021-06-08
 */
@Slf4j
@Service(value = "forexAccountService")
public class ForexAccountServiceImpl implements ForexAccountService {

    @Autowired
    ForexAccountMapper forexAccountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @HmilyTCC(confirmMethod = "confirmExchange", cancelMethod = "cancelExchange")
    public Object exchangeMoney(ForexAccount forexAccount) {
        UpdateWrapper<ForexAccount> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("username", forexAccount.getUsername());
        return forexAccountMapper.exchangeMoney(forexAccount);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirmExchange(ForexAccount forexAccount) {
        log.info("======== Hmily TCC confirm the money exchange operation ========");
        log.info("Account info: " + forexAccount.toString());
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancelExchange(ForexAccount forexAccount) {
        log.info("======== Hmily TCC cancel the money exchange operation ========");
        log.info("Account info: " + forexAccount.toString());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object save(ForexAccount entity) {
        return forexAccountMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object remove(QueryWrapper<ForexAccount> queryWrapper) {
        return forexAccountMapper.delete(queryWrapper);
    }
}
```

**后台mybatis持久层mapper**

**使用自定义sql实现外币兑换逻辑**

```java
@Update("update forex_account set us_wallet = us_wallet + #{usWallet}, cny_wallet = cny_wallet +" +
            "#{cnyWallet} where us_wallet >= #{usWallet} and cny_wallet >= #{cnyWallet} and username = #{username}")
```



```java
package com.nerotomto.forex.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import forex.account.api.entity.ForexAccount;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 用户外汇账号金额表 Mapper 接口
 * </p>
 *
 * @author nero
 * @since 2021-06-08
 */
public interface ForexAccountMapper extends BaseMapper<ForexAccount> {
    /**
     * 自定义sql
     */
    @Update("update forex_account set us_wallet = us_wallet + #{usWallet}, cny_wallet = cny_wallet +" +
            "#{cnyWallet} where us_wallet >= #{usWallet} and cny_wallet >= #{cnyWallet} and username = #{username}")
    int exchangeMoney(ForexAccount forexAccount);

}
```

# 5.服务调用方Consumer

**exchange-demo**

**调用方dubbo配置spring-dubbo-consumer.xml**

```xml
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://dubbo.apache.org/schema/dubbo"
       xmlns="http://www.springframework.org/schema/beans"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://dubbo.apache.org/schema/dubbo http://dubbo.apache.org/schema/dubbo/dubbo.xsd">

    <dubbo:application name="exchange-service-consumer"/>

    <dubbo:registry address="zookeeper://127.0.0.1:2181"/>

    <dubbo:protocol name="dubbo" port="20881"
                    server="netty" client="netty"
                    charset="UTF-8" threadpool="fixed" threads="500"
                    queues="0" buffer="8192" accepts="0" payload="8388608"/>

    <dubbo:reference id="forexAccountService" check="false" interface="forex.account.api.service.ForexAccountService"/>

</beans>
```

**调用方hmily配置hmily.yml**

```yaml
hmily:
  server:
    configMode: local
    appName: exchange demo service
  #  如果server.configMode eq local 的时候才会读取到这里的配置信息.
  config:
    appName: exchange demo service
    serializer: kryo
    contextTransmittalMode: threadLocal
    scheduledThreadMax: 16
    scheduledRecoveryDelay: 60
    scheduledCleanDelay: 60
    scheduledPhyDeletedDelay: 600
    scheduledInitDelay: 30
    recoverDelayTime: 60
    cleanDelayTime: 180
    limit: 200
    retryMax: 10
    bufferSize: 8192
    consumerThreads: 16
    asyncRepository: true
    autoSql: true
    phyDeleted: true
    storeDays: 3
    repository: mysql

repository:
  database:
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/hmily?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    maxActive: 20
    minIdle: 10
    connectionTimeout: 30000
    idleTimeout: 600000
    maxLifetime: 1800000
  file:
    path:
    prefix: /hmily
  mongo:
    databaseName:
    url:
    userName:
    password:
  zookeeper:
    host: localhost:2181
    sessionTimeOut: 1000
    rootPath: /hmily
  redis:
    cluster: false
    sentinel: false
    clusterUrl:
    sentinelUrl:
    masterName:
    hostName:
    port:
    password:
    maxTotal: 8
    maxIdle: 8
    minIdle: 2
    maxWaitMillis: -1
    minEvictableIdleTimeMillis: 1800000
    softMinEvictableIdleTimeMillis: 1800000
    numTestsPerEvictionRun: 3
    testOnCreate: false
    testOnBorrow: false
    testOnReturn: false
    testWhileIdle: false
    timeBetweenEvictionRunsMillis: -1
    blockWhenExhausted: true
    timeOut: 1000

metrics:
  metricsName: prometheus
  host:
  port: 9081
  async: true
  threadCount: 16
  jmxConfig:
```

**调用方application.yml配置**

**使用hmily的话，这里需要配置数据源，否则会报错**

```yaml
server:
  port: 8081

spring:
  #配置分库分表策略
  shardingsphere:
    datasource:
      common:
        driver-class-name: com.mysql.cj.jdbc.Driver
        type: com.zaxxer.hikari.HikariDataSource
      ds0:
        autoCommit: false
        jdbc-url: jdbc:mysql://localhost:3306/money_0?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        password: 123456
        username: root
      ds1:
        autoCommit: false
        jdbc-url: jdbc:mysql://localhost:3306/money_1?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
        password: 123456
        username: root
      names: ds0,ds1
    rules:
      props:
        sql:
          show: true
      sharding:
        key-generators:
          snowflake:
            props:
              worker-id: 33
            type: snowflake
        sharding-algorithms:
          database-inline:
            props:
              algorithm-expression: ds$->{id % 2}
            type: INLINE
          table-inline:
            props:
              algorithm-expression: forex_account_$->{id % 2}
            type: INLINE
        tables:
          #由于整合了mybatis-plus，所以这里逻辑表名称改成跟实际表名称一致
          forex_account:
            actual-data-nodes: ds$->{0..1}.forex_account_$->{0..1}
            database-strategy:
              standard:
                sharding-algorithm-name: database-inline
                sharding-column: id
            key-generate-strategy:
              column: id
              key-generator-name: snowflake
            key-generator:
              column: id
              props:
                worker:
                  id: 33
              type: SNOWFLAKE
            table-strategy:
              standard:
                sharding-algorithm-name: table-inline
                sharding-column: id

swagger:
  application-description: Dubbo Hmily-Tcc
  application-name: Exchange Demo Service
  application-version: 1.0
  enable: true
  try-host: http://localhost:${server.port}

logging:
  level:
    root: info
```

**调用方consumer主类**

```java
package com.nerotomato.exchange;

import com.nerotomato.exchange.service.ExchangeService;
import forex.account.api.entity.ForexAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

import java.math.BigDecimal;

/**
 * Created by nero on 2021/6/9.
 */
@SpringBootApplication
@ImportResource({"classpath:spring-dubbo-consumer.xml"})
public class ExchangeDemoApplication implements ApplicationRunner {
    @Autowired
    private ExchangeService exchangeService;

    public static void main(String[] args) {
        SpringApplication.run(ExchangeDemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        ForexAccount forexAccount = new ForexAccount();
        forexAccount.setUsername("nero");
        forexAccount.setCnyWallet(new BigDecimal("7"));
        forexAccount.setUsWallet(new BigDecimal("-1"));

        ForexAccount forexAccount1 = new ForexAccount();
        forexAccount1.setUsername("dante");
        forexAccount1.setCnyWallet(new BigDecimal("-7"));
        forexAccount1.setUsWallet(new BigDecimal(1));
        exchangeService.exchange(forexAccount, forexAccount1);
    }
}
```

**调用方本地service类ExchangeServiceImpl**

**在ExchangeServiceImpl类中调用远程服务ForexAccountService**

```java
package com.nerotomato.exchange.service.impl;

import com.nerotomato.exchange.service.ExchangeService;
import forex.account.api.entity.ForexAccount;
import forex.account.api.service.ForexAccountService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by nero on 2021/6/9.
 */
@Slf4j
@Service
public class ExchangeServiceImpl implements ExchangeService {

    @Autowired
    ForexAccountService forexAccountService;

    @Override
    @HmilyTCC(confirmMethod = "confirmExchangeOperation", cancelMethod = "cancelExchangeOperation")
    public Object exchange(ForexAccount forexAccount1, ForexAccount forexAccount2) {
        Object result1 = exchangeRemote(forexAccount1);
        Object result2 = exchangeRemote(forexAccount2);
        return result1.toString() + "," + result2.toString();
    }

    /**
     * dubbo调用远程服务
     */
    private Object exchangeRemote(ForexAccount forexAccount) {
        return forexAccountService.exchangeMoney(forexAccount);
    }

    public boolean confirmExchangeOperation(ForexAccount forexAccount1, ForexAccount forexAccount2) {
        log.info("confirm the exchange operation.");
        return true;
    }

    public boolean cancelExchangeOperation(ForexAccount forexAccount1, ForexAccount forexAccount2) {
        log.info("cancel the exchange operation!!!");
        return true;
    }
}
```

