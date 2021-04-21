package com.nerotomato.jdbc;

import java.sql.*;

/**
 * JDBC加PreparedStatement sql预处理对象
 * 可以防止sql注入
 * Created by nero on 2021/4/20.
 */
public class JDBCDemo2 {
    private static String url = "jdbc:mysql://localhost:3306/mybatis-test?characterEncoding=utf-8";
    private static String username = "root";
    private static String password = "root123";
    private static String insertSql = "INSERT INTO user\n" +
            "(user_name, password, name, age, sex, birthday, created, updated)\n" +
            "VALUES(?, ?, ?, ?, 1, NOW() ,NOW(), NOW())";
    private static String deleteSql = "delete from user where user_name=?";
    private static String updateSql = "update user set password=? where user_name=?";
    private static String querySql = "select * from user where user_name = ?";

    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement pstat = null;
        ResultSet resultSet = null;
        try {
            //加载数据库驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            //创建连接
            conn = DriverManager.getConnection(url, username, password);
            //创建sql执行预处理对象

            //执行sql查询语句
            //pstat = conn.prepareStatement(querySql);
             /* pstat.setObject(1, "test or 1=1");
            resultSet = pstat.executeQuery();
            while (resultSet.next()) {
                System.out.println("user_name: " + resultSet.getString(2));
                System.out.println("name: " + resultSet.getString(4));
                System.out.println("age: " + resultSet.getInt(5));
            }*/

            //执行sql新增语句
            //pstat = conn.prepareStatement(insertSql);
            /*pstat.setObject(1,"test");
            pstat.setObject(2,"123456789");
            pstat.setObject(3,"测试用户");
            pstat.setObject(4,26);
            int result = pstat.executeUpdate();
            System.out.println(result);*/

            //执行sql更新语句
            //pstat = conn.prepareStatement(updateSql);
            /*pstat.setObject(1, "987654321");
            pstat.setObject(2, "test");
            int result = pstat.executeUpdate();
            System.out.println(result);*/

            //执行删除sql
            /*pstat = conn.prepareStatement(deleteSql);
            pstat.setObject(1,"test");
            int result = pstat.executeUpdate();
            System.out.println(result);*/


            //批处理
            pstat = conn.prepareStatement(insertSql);

            for (int i = 0; i < 10000; i++) {
                pstat.setObject(1, "test" + i);
                pstat.setObject(2, "123456789");
                pstat.setObject(3, "测试用户" + i);
                pstat.setObject(4, 26);
                pstat.addBatch();
            }
            pstat.executeBatch();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (pstat != null) {
                    pstat.close();
                }
                if (conn != null) {
                    conn.close();

                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
