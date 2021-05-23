#基于shardingsphere-proxy的mysql读写分离实现方案,并使用docker容器部署
1.创建shardingsphere-proxy容器

```shell
docker run -d -v D:/DockerDesktop/dockerContainer/shardingsphere-proxy/conf:/opt/sharding-proxy/conf -v D:/DockerDesktop/dockerContainer/shardingsphere-proxy/ext-lib:/opt/sharding-proxy/ext-lib --name sharding-proxy -e PORT=3308 -p13308:3308 apache/sharding-proxy:latest
```

2.将shardingsphere-proxy容器连接到自定义桥接网络my-bridge

```shell
docker network connect my-bridge sharding-proxy
```

3.将shardingsphere-proxy容器与默认桥接网络断开连接

```shell
docker network disconnect bridge sharding-proxy
```

4.server.yaml配置

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
  acceptor-size: 16  # The default value is available processors count * 2.
  executor-size: 16  # Infinite by default.
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

5.config-sharding.yaml配置

-    - ```yaml
       schemaName: tomato_shop_proxy
       
       dataSources:
          master_ds:
            url: jdbc:mysql://172.18.0.3:3306/tomato_shop?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
            username: root
            password: 123456
            connectionTimeoutMilliseconds: 30000
            idleTimeoutMilliseconds: 60000
            maxLifetimeMilliseconds: 1800000
            maxPoolSize: 50
            minPoolSize: 1
            maintenanceIntervalMilliseconds: 30000
          slave_ds_0:
            url: jdbc:mysql://172.18.0.2:3306/tomato_shop?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
            username: root
            password: 123456
            connectionTimeoutMilliseconds: 30000
            idleTimeoutMilliseconds: 60000
            maxLifetimeMilliseconds: 1800000
            maxPoolSize: 50
            minPoolSize: 1
            maintenanceIntervalMilliseconds: 30000
       
       rules:
       - !REPLICA_QUERY
         dataSources:
           master_ds:
             primaryDataSourceName: master_ds
             replicaDataSourceNames:
               - slave_ds_0
       ```