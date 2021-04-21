# 1.连接池实现说明

JDBC连接池的实现并不复杂，主要是对JDBC中几个核心对象Connection、Statement、PreparedStatement、CallableStatement以及ResultSet的封装与动态代理

# 2.连接池种类

C3P0,BoneCP,DBCP,Druid,HikariCP等等