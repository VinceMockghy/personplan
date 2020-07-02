package cn.edu.zucc.personplan.util;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;



public class DBUtil_pool {
    private static DBUtil_pool dbUtil2;
    static {
        dbUtil2 = new DBUtil_pool();
    }
    private static ComboPooledDataSource dataSource;

    public DBUtil_pool() {
        try {
            dataSource = new ComboPooledDataSource();
            dataSource.setUser("root");
            dataSource.setPassword("123abc456d");
            dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/personplan?useSSL=false");
            dataSource.setDriverClass("com.mysql.jdbc.Driver");
            dataSource.setInitialPoolSize(2);
            dataSource.setMinPoolSize(1);
            dataSource.setMaxPoolSize(10);
            dataSource.setMaxStatements(50);
            dataSource.setMaxIdleTime(60);
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }
    }

    public final static DBUtil_pool getInstance() {
        return dbUtil2;
    }

    public final static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("无法连接数据库");
        }
    }
}