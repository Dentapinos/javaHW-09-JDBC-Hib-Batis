package org.example.configs;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class SessionManager {
    private static final SessionFactory hibernateSessionFactory = HibernateManager.getSessionFactory();
    private static final SqlSessionFactory myBatisSessionFactory = MyBatisConfig.getSessionFactory();

    public static Object createSession(String type) {
        return switch (type.toLowerCase()) {
            case "hibernate" -> openHibernateSession();
            case "jdbc" -> getConnectionJDBC();
            case "mybatis" -> openMyBatisSession();
            default -> throw new IllegalArgumentException("Такого соединения нет: " + type);
        };
    }

    private static Session openHibernateSession() {
        try {
            return hibernateSessionFactory.openSession();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Не удалось открыть Hibernate сессию", e);
        }
    }

    private static SqlSession openMyBatisSession() {
        try {
            return myBatisSessionFactory.openSession();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Не удалось открыть MyBatis сессию", e);
        }
    }

    private static Connection getConnectionJDBC() {
        try {
            try {
                return JDBCManager.getDBConnection();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Не удалось получить JDBC соединение", e);
        }
    }

}
