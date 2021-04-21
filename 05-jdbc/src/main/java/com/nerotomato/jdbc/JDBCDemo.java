package com.nerotomato.jdbc;

import java.sql.*;
import java.util.Queue;

/**
 * 原生JDBC接口
 * Created by nero on 2021/4/20.
 */
public class JDBCDemo {
    private static String url = "jdbc:mysql://localhost:3306/mybatis-test";
    private static String username = "root";
    private static String password = "root123";
    private static String insertSql = "INSERT INTO user\n" +
            "(user_name, password, name, age, sex, birthday, created, updated)\n" +
            "VALUES('test', '123456', '测试用户', 25, 1, NOW() ,NOW(), NOW())";
    private static String deleteSql = "delete from user where user_name='test'";
    private static String updateSql = "update user set password='987654321' where user_name='test'";
    private static String querySql = "select * from user where user_name = 'test' or 1=1";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stat = null;
        ResultSet resultSet = null;
        try {
            //加载数据库驱动
            //Class.forName("com.mysql.jdbc.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
            //创建连接
            conn = DriverManager.getConnection(url, username, password);
            //创建sql执行对象
            stat = conn.createStatement();
            //执行sql语句
            //返回值int,操作成功的数据库的行数
            //int result = stat.executeUpdate(insertSql);
            //int result = stat.executeUpdate(updateSql);
            //int result = stat.executeUpdate(deleteSql);
            //System.out.println(result);
            resultSet = stat.executeQuery(querySql);
            while (resultSet.next()) {
                System.out.println("user_name: " + resultSet.getString(2));
                System.out.println("name: " + resultSet.getString(4));
                System.out.println("age: " + resultSet.getInt(5));
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (stat != null) {
                    stat.close();
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
