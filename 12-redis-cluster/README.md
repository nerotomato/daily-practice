[TOC]

# 1.docker创建redis服务

**windows环境dockerDesktop创建redis服务**

```shell
docker run -v D:/DockerDesktop/dockerContainer/redis/data:/data ^
-v D:/DockerDesktop/dockerContainer/redis/redis.conf:/etc/redis/redis.conf ^
-e TZ=Asia/Shanghai ^
-p 6379:6379 --name redis -d redis:latest redis-server --appendonly yes
```

**linux环境docker创建redis服务**

```shell
docker run -v /home/nerotomato/Develop/docker/redis:/data \
-v /home/nerotomato/Develop/docker/redis/redis.conf:/etc/redis/redis.conf \
-e TZ=Asia/Shanghai \
-p 6379:6379 --name redis -d redis:latest redis-server --appendonly yes
```



# 2.性能测试

使用redis自带命令redis-benchmark测试

```shell
redis-benchmark -n 100000 -c 32 -t SET,GET,INCR,HSET,LPUSH,MSET -q
```



redis 性能测试工具可选参数如下所示：
1	-h	指定服务器主机名	127.0.0.1
2	-p	指定服务器端口	6379
3	-s	指定服务器 socket	
4	-c	指定并发连接数	50
5	-n	指定请求数	10000
6	-d	以字节的形式指定 SET/GET 值的数据大小	2
7	-k	1=keep alive 0=reconnect	1
8	-r	SET/GET/INCR 使用随机 key, SADD 使用随机值	
9	-P	通过管道传输 请求	1
10	-q	强制退出 redis。仅显示 query/sec 值	
11	–csv	以 CSV 格式输出	
12	-l	生成循环，永久执行测试	
13	-t	仅运行以逗号分隔的测试命令列表。	
14	-I	Idle 模式。仅打开 N 个 idle 连接并等待。



# 3.主从复制

**一主两从**

```shell
docker run -v /home/nerotomato/Develop/docker/redis-1/data:/data \
-v /home/nerotomato/Develop/docker/redis-1/redis.conf:/etc/redis/redis.conf \
-e TZ=Asia/Shanghai \
--net my-bridge \
--ip 172.18.0.5 \
-p 6379:6379 --name redis-1 -d redis:latest redis-server --appendonly yes

docker run -v /home/nerotomato/Develop/docker/redis-2/data:/data \
-v /home/nerotomato/Develop/docker/redis-2/redis.conf:/etc/redis/redis.conf \
-e TZ=Asia/Shanghai \
--net my-bridge \
--ip 172.18.0.6 \
-p 6380:6379 --name redis-2 -d redis:latest redis-server --appendonly yes

docker run -v /home/nerotomato/Develop/docker/redis-3/data:/data \
-v /home/nerotomato/Develop/docker/redis-3/redis.conf:/etc/redis/redis.conf \
-e TZ=Asia/Shanghai \
--net my-bridge \
--ip 172.18.0.7 \
-p 6381:6379 --name redis-3 -d redis:latest redis-server --appendonly yes
```

**登陆两个从库执行下列命令**

**分别设置主库的IP和端口：172.18.0.5 6379** 

```shell
docker exec -it redis-2 /bin/bash
cd /usr/local/bin && ./redis-cli
slaveof 172.18.0.5 6379 
```

```shell
docker exec -it redis-3 /bin/bash
cd /usr/local/bin && ./redis-cli
slaveof 172.18.0.5 6379
```

**可通过role和info replication命令查看从库状态**

```shell
role
info replication
```



# 4.搭建高可用集群sentinel 哨兵模式（Redis-Sentinel）

**创建3个哨兵服务**

```shell
docker run -v /home/nerotomato/Develop/docker/redis-sentinel-1/sentinel.conf:/etc/redis/sentinel.conf \
-e TZ=Asia/Shanghai \
--net my-bridge \
--ip 172.18.0.8 \
-p 26379:26379 --name redis-sentinel-1 -d redis:latest redis-sentinel /etc/redis/sentinel.conf
```

```shell
docker run -v /home/nerotomato/Develop/docker/redis-sentinel-2/sentinel.conf:/etc/redis/sentinel.conf \
-e TZ=Asia/Shanghai \
--net my-bridge \
--ip 172.18.0.9 \
-p 26380:26379 --name redis-sentinel-2 -d redis:latest redis-sentinel /etc/redis/sentinel.conf
```

```shell
docker run -v /home/nerotomato/Develop/docker/redis-sentinel-3/sentinel.conf:/etc/redis/sentinel.conf \
-e TZ=Asia/Shanghai \
--net my-bridge \
--ip 172.18.0.10 \
-p 26381:26379 --name redis-sentinel-3 -d redis:latest redis-sentinel /etc/redis/sentinel.conf
```



**sentinel配置文件配置sentinel.conf**

**这里使用的是自定义的桥接网络my-bridge,所以redis的mymaster ip是172.18.0.5**

**如果使用的是host网络模式，即使用宿主机网络，则mymaster的ip改成127.0.0.1**

```properties
# 哨兵sentinel实例运行的端口 默认26379
port 26379

# 哨兵sentinel的工作目录
dir "/tmp"

# 哨兵sentinel监控的redis主节点的 ip port
# master-name  可以自己命名的主节点名字 只能由字母A-z、数字0-9 、这三个字符".-_"组成。
# quorum 当这些quorum个数sentinel哨兵认为master主节点失联 那么这时 客观上认为主节点失联了
# sentinel monitor <master-name> <ip> <redis-port> <quorum>

sentinel monitor mymaster 172.18.0.5 6379 2

# 当在Redis实例中开启了requirepass foobared 授权密码 这样所有连接Redis实例的客户端都要提供密码
# 设置哨兵sentinel 连接主从的密码 注意必须为主从设置一样的验证密码
# sentinel auth-pass <master-name> <password>

# 指定多少毫秒之后 主节点没有应答哨兵sentinel 此时 哨兵主观上认为主节点下线 默认30秒
# sentinel down-after-milliseconds <master-name> <milliseconds>

sentinel down-after-milliseconds mymaster 30000

# 这个配置项指定了在发生failover主备切换时最多可以有多少个slave同时对新的master进行 同步，
#这个数字越小，完成failover所需的时间就越长，
#但是如果这个数字越大，就意味着越 多的slave因为replication而不可用。
#可以通过将这个值设为 1 来保证每次只有一个slave 处于不能处理命令请求的状态。
# sentinel parallel-syncs <master-name> <numslaves>

sentinel parallel-syncs mymaster 1

# 故障转移的超时时间 failover-timeout 可以用在以下这些方面：
#1. 同一个sentinel对同一个master两次failover之间的间隔时间。
#2. 当一个slave从一个错误的master那里同步数据开始计算时间。直到slave被纠正为向正确的master那里同步数据时。
#3.当想要取消一个正在进行的failover所需要的时间。
#4.当进行failover时，配置所有slaves指向新的master所需的最大时间。不过，即使过了这个超时，slaves依然会被正确配置为指向master，但是就不按parallel-syncs所配置的规则来了
# 默认三分钟
# sentinel failover-timeout <master-name> <milliseconds>

sentinel failover-timeout mymaster 180000
```

**手动停止redis-1模拟主库宕机的情况**

**查看sentinel日志，30秒后看到主机master发生了切换**

```shell
docker stop redis-1
docker logs -f redis-sentinel-1
docker logs -f redis-sentinel-2
docker logs -f redis-sentinel-3
```

**日志信息**

```
1:X 26 Jun 2021 23:47:03.093 # +sdown slave 172.18.0.5:6379 172.18.0.5 6379 @ mymaster 172.18.0.6 6379
1:X 26 Jun 2021 23:49:59.523 # -sdown slave 172.18.0.5:6379 172.18.0.5 6379 @ mymaster 172.18.0.6 6379
1:X 26 Jun 2021 23:50:09.547 * +convert-to-slave slave 172.18.0.5:6379 172.18.0.5 6379 @ mymaster 172.18.0.6 6379
```



# 5.搭建redis cluster集群



## **5.1.创建redis-cluster目录，用于存放redis集群的各节点配置文件和数据**

```shell
mkdir -p /home/nerotomato/Develop/docker/redis-cluster
```

**切换至指定目录,编写 redis-cluster.tmpl 文件** 

```shell
cd /home/nerotomato/Develop/docker/redis-cluster
vim redis-cluster.tmpl

```

**输入下列内容**

```properties
port ${PORT}
protected-mode no
daemonize no
appendonly yes
cluster-enabled yes
cluster-config-file nodes.conf
cluster-node-timeout 15000
cluster-announce-ip 127.0.0.1
cluster-announce-port ${PORT}
cluster-announce-bus-port 1${PORT}
```

**配置详解**

```
port：节点端口；
requirepass：添加访问认证；
masterauth：如果主节点开启了访问认证，从节点访问主节点需要认证；
protected-mode：保护模式，默认值 yes，即开启。开启保护模式以后，需配置 bind ip 或者设置访问密码；关闭保护模式，外部网络可以直接访问；
daemonize：是否以守护线程的方式启动（后台启动），默认 no；
appendonly：是否开启 AOF 持久化模式，默认 no；
cluster-enabled：是否开启集群模式，默认 no；
cluster-config-file：集群节点信息文件；
cluster-node-timeout：集群节点连接超时时间；
cluster-announce-ip：集群节点 IP，填写宿主机的 IP；
cluster-announce-port：集群节点映射端口；
cluster-announce-bus-port：集群节点总线端口。
```

**每个 Redis 集群节点都需要打开两个 TCP 连接。一个用于为客户端提供服务的正常 Redis TCP 端口，例如 6379。还有一个基于 6379 端口加 10000 的端口，比如 16379。第二个端口用于集群总线，这是一个使用二进制协议的节点到节点通信通道。节点使用集群总线进行故障检测、配置更新、故障转移授权等等。客户端永远不要尝试与集群总线端口通信，与正常的 Redis 命令端口通信即可，但是请确保防火墙中的这两个端口都已经打开，否则 Redis 集群节点将无法通信。**



**在/home/nerotomato/Develop/docker/redis-cluster目录执行下列命令**

**创建6个文件夹，分别是6171-6376，用于存放redis节点数据**

```shell
for port in `seq 6371 6376`; do \
  mkdir -p ${port}/conf \
  && PORT=${port} envsubst < redis-cluster.tmpl > ${port}/conf/redis.conf \
  && mkdir -p ${port}/data;\
done
```

## **5.2.创建redis容器**

**将宿主机的 `6371 ~ 6376` 之间的端口与 6 个 Redis 容器映射，并将宿主机的目录与容器内的目录进行映射（目录挂载）。记得指定网络模式，使用 `host` 网络模式。**

```shell
for port in $(seq 6371 6376); do \
  docker run -di --name redis-${port} --net host \
  -v /home/nerotomato/Develop/docker/redis-cluster/${port}/conf/redis.conf:/usr/local/etc/redis/redis.conf \
  -v /home/nerotomato/Develop/docker/redis-cluster/${port}/data:/data \
  redis redis-server /usr/local/etc/redis/redis.conf; \
done
```

## **5.3.创建redis-cluster集群**



**进入容器 **

**切换至指定目录,执行创建集群命令，由于是本地机器测试，并没有实际在多台机器操作，所以**

**这里会在本地一台机器创建3个主库master（6371-6373），3个从库slave（6374-6376）**

```shell
docker exec -it redis-6371 /bin/bash
cd /usr/local/bin/
 
redis-cli --cluster create 127.0.0.1:6371 127.0.0.1:6372 127.0.0.1:6373 127.0.0.1:6374 127.0.0.1:6375 127.0.0.1:6376 --cluster-replicas 1
```

**下面是执行上述命令后显示的日志信息，中间会有一次确认操作，输入yes即可**

```shell
root@nerotomato:/usr/local/bin# redis-cli --cluster create 127.0.0.1:6371 127.0.0.1:6372 127.0.0.1:6373 127.0.0.1:6374 127.0.0.1:6375 127.0.0.1:6376 --cluster-replicas 1
>>> Performing hash slots allocation on 6 nodes...
Master[0] -> Slots 0 - 5460
Master[1] -> Slots 5461 - 10922
Master[2] -> Slots 10923 - 16383
Adding replica 127.0.0.1:6375 to 127.0.0.1:6371
Adding replica 127.0.0.1:6376 to 127.0.0.1:6372
Adding replica 127.0.0.1:6374 to 127.0.0.1:6373
>>> Trying to optimize slaves allocation for anti-affinity
[WARNING] Some slaves are in the same host as their master
M: 5ac40fbb942341ff1ab420f002736f089840e2d2 127.0.0.1:6371
   slots:[0-5460] (5461 slots) master
M: 43e0f5464e286ee355fd6a2a510746c3e1e6117c 127.0.0.1:6372
   slots:[5461-10922] (5462 slots) master
M: 5cd41f746cb2c9618a38665e2503045ac0c7da2b 127.0.0.1:6373
   slots:[10923-16383] (5461 slots) master
S: ac0a1d341e0e93bdab8bbf4942f5dbf92b902eaa 127.0.0.1:6374
   replicates 5ac40fbb942341ff1ab420f002736f089840e2d2
S: 742eca13bce6621822f85342068b86b9da4bdd99 127.0.0.1:6375
   replicates 43e0f5464e286ee355fd6a2a510746c3e1e6117c
S: 7b7b188fcf3d4521ed9b3123a8cb6048dc7050d4 127.0.0.1:6376
   replicates 5cd41f746cb2c9618a38665e2503045ac0c7da2b
Can I set the above configuration? (type 'yes' to accept): yes
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join
.
>>> Performing Cluster Check (using node 127.0.0.1:6371)
M: 5ac40fbb942341ff1ab420f002736f089840e2d2 127.0.0.1:6371
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
S: ac0a1d341e0e93bdab8bbf4942f5dbf92b902eaa 127.0.0.1:6374
   slots: (0 slots) slave
   replicates 5ac40fbb942341ff1ab420f002736f089840e2d2
M: 43e0f5464e286ee355fd6a2a510746c3e1e6117c 127.0.0.1:6372
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
S: 7b7b188fcf3d4521ed9b3123a8cb6048dc7050d4 127.0.0.1:6376
   slots: (0 slots) slave
   replicates 5cd41f746cb2c9618a38665e2503045ac0c7da2b
S: 742eca13bce6621822f85342068b86b9da4bdd99 127.0.0.1:6375
   slots: (0 slots) slave
   replicates 43e0f5464e286ee355fd6a2a510746c3e1e6117c
M: 5cd41f746cb2c9618a38665e2503045ac0c7da2b 127.0.0.1:6373
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.

```

**至此一个高可用的 Redis Cluster 集群搭建完成，该集群中包含 6 个 Redis 节点，3 主 3 从。三个主节点会分配槽，处理客户端的命令请求，而从节点可用在主节点故障后，顶替主节点。**



## **5.4.查看集群状态**

```shell
# 进入容器
docker exec -it redis-6371 /bin/bash
# 切换至指定目录
cd /usr/local/bin/

redis-cli --cluster check 127.0.0.1:6371
redis-cli --cluster check 127.0.0.1:6372
redis-cli --cluster check 127.0.0.1:6373
redis-cli --cluster check 127.0.0.1:6374
redis-cli --cluster check 127.0.0.1:6375
redis-cli --cluster check 127.0.0.1:6376
```

## **5.5.查看集群节点信息**

```shell
# 连接至集群某个节点
redis-cli -c -h 127.0.0.1 -p 6376
# 查看集群信息
cluster info
# 查看集群结点信息
cluster nodes
```

## **5.6.测试SET/GET键值对**

```shell
# 连接至集群某个节点
redis-cli -c -h 127.0.0.1 -p 6371

set a 123
set b 456
set apple macbookpro
set job javadeveloper
```

**查看显示日志，可以看到a和b两个key 进过hash运算后，根据槽位信息分别是15495和3300，分别被分配到6373和6371两个节点**

```shell
127.0.0.1:6371> set a 123
-> Redirected to slot [15495] located at 127.0.0.1:6373
OK
127.0.0.1:6373> set b 456
-> Redirected to slot [3300] located at 127.0.0.1:6371
OK
127.0.0.1:6371> set apple macbookpro
-> Redirected to slot [7092] located at 127.0.0.1:6372
OK
127.0.0.1:6372> set job javadeveloper
-> Redirected to slot [2906] located at 127.0.0.1:6371
OK
```
