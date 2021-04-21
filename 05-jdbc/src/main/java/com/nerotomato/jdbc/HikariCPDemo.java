package com.nerotomato.jdbc;

import com.nerotomato.utils.HikariUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 使用HikariCP连接池进行JDBC操作
 * Created by nero on 2021/4/21.
 */
public class HikariCPDemo {
    private static String insertSql = "INSERT INTO user\n" +
            "(user_name, password, name, age, sex, birthday, created, updated)\n" +
            "VALUES(?, ?, ?, ?, 1, NOW() ,NOW(), NOW())";
    private static String deleteSql = "delete from user where user_name like ?";
    private static String updateSql = "update user set password=? where user_name like ?";
    private static String querySql = "select * from user where user_name like ?";

    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement pstat = null;
        ResultSet resultSet = null;
        try {
            conn = HikariUtil.getConnection();
            //开启事务
            HikariUtil.beginTransaction(conn);

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

            //删除操作
            /*pstat = conn.prepareStatement(deleteSql);
            pstat.setObject(1, "%test%");
            int result = pstat.executeUpdate();
            System.out.println(result);*/

            //查询操作
            /*pstat = conn.prepareStatement(querySql);
            pstat.setObject(1, "%test%");
            resultSet = pstat.executeQuery();
            while (resultSet.next()) {
                System.out.println("user_name: " + resultSet.getString(2));
                System.out.println("name: " + resultSet.getString(4));
                System.out.println("age: " + resultSet.getInt(5));
            }*/

            //更新操作
            /*pstat = conn.prepareStatement(updateSql);
            pstat.setObject(1, "987654321");
            pstat.setObject(2, "%test%");
            int result = pstat.executeUpdate();
            System.out.println(result);*/

            //提交事务
            HikariUtil.commitTransaction(conn);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            //回滚事务
            HikariUtil.rollBackTransaction(conn);
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
