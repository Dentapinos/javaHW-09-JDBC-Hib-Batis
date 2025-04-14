package org.example.configs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCManager {
    public static Connection getDBConnection() throws ClassNotFoundException, SQLException {
        //подтягивает драйвер внутрь приложения
        Class.forName("com.mysql.cj.jdbc.Driver");
        //создаем соединение с базой данных
        String bdTableName = "tt"; //название таблицы в базе
        String bdLogin = "root";    //логин
        String bdPassword = "den27lad27";   //пароль
        String multipleStatement = "allowMultiQueries=true";
        String createDatabaseIfNotExist = "createDatabaseIfNotExist=true";  //создание базы данных если она не существует
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + bdTableName + "?" + multipleStatement + "&user=" + bdLogin + "&password=" + bdPassword + "&" + createDatabaseIfNotExist);
        return conn;
    }
}