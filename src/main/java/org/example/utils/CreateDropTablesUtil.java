package org.example.utils;

import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDropTablesUtil {

    public static void dropAndCreateNewTables(Connection conn) {
        dropAllTables(conn);
        createAllTables(conn);
    }

    public static void dropAndCreateNewTables(SqlSession sqlSession) {
        dropAllTables(sqlSession);
        createAllTables(sqlSession);
    }


    public static void createAllTables(Connection conn) {
        try {
            createBrandModel(conn);
            createEmployeeTask(conn);
            createStreetHouseTables(conn);
            createMasterKittyTables(conn);
        } catch (SQLException e) {
            throw new RuntimeException("JDBC - Ошибка создания таблиц: " + e.getMessage());
        }
    }

    public static void createAllTables(SqlSession sqlSession) {
        Connection connection = sqlSession.getConnection();
        try {
            createBrandModel(connection);
            createEmployeeTask(connection);
            createStreetHouseTables(connection);
            createMasterKittyTables(connection);
        } catch (SQLException e) {
            throw new RuntimeException("MyBatis - Ошибка создания таблиц: " + e.getMessage());
        }
    }

    public static void dropAllTables(Connection conn) {
        try {
            dropBrandModelTables(conn);
            dropEmployeeTaskTables(conn);
            dropMasterKittyTables(conn);
            dropStreetHoseTables(conn);
        } catch (SQLException e) {
            throw new RuntimeException("JDBC - Ошибка удаления таблиц: " + e.getMessage());
        }
    }

    public static void dropAllTables(SqlSession sqlSession) {
        Connection connection = sqlSession.getConnection();
        try {
            dropBrandModelTables(connection);
            dropEmployeeTaskTables(connection);
            dropMasterKittyTables(connection);
            dropStreetHoseTables(connection);
        } catch (SQLException e) {
            throw new RuntimeException("MyBatis - Ошибка удаления таблиц: " + e.getMessage());
        }
    }


    public static void dropMasterKittyTables(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            String dropMastersKittiesTable = "DROP TABLE IF EXISTS master_kitty";
            String dropKittiesTable = "DROP TABLE IF EXISTS kitties";
            String dropMastersTable = "DROP TABLE IF EXISTS masters";

            statement.execute(dropMastersKittiesTable);
            statement.execute(dropKittiesTable);
            statement.execute(dropMastersTable);
        }
    }

    public static void dropBrandModelTables(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            String dropModelTable = "DROP TABLE IF EXISTS models_car";
            String dropBrandTable = "DROP TABLE IF EXISTS brands_car";

            statement.execute(dropModelTable);
            statement.execute(dropBrandTable);
        }
    }

    public static void dropStreetHoseTables(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            String dropHouseTable = "DROP TABLE IF EXISTS houses";
            String dropStreetTable = "DROP TABLE IF EXISTS streets";

            statement.execute(dropHouseTable);
            statement.execute(dropStreetTable);
        }
    }

    public static void dropEmployeeTaskTables(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            String dropTaskTable = "DROP TABLE IF EXISTS tasks";
            String dropEmployeeTable = "DROP TABLE IF EXISTS employees";

            statement.execute(dropTaskTable);
            statement.execute(dropEmployeeTable);
        }
    }


    private static void createMasterKittyTables(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            String sqlCreateTableKitty = """
                    CREATE TABLE IF NOT EXISTS kitties
                    (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NULL,
                        birthday DATE NULL,
                        breed VARCHAR(255) NULL,
                        color ENUM ('WHITE', 'BLACK', 'RED_HAIRED', 'BROWN', 'GREY') DEFAULT 'WHITE'
                    )
                    """;

            String sqlCreateTableMaster = """
                    CREATE TABLE IF NOT EXISTS masters
                    (
                        id       INT AUTO_INCREMENT PRIMARY KEY,
                        name     VARCHAR(255) NULL,
                        birthday DATE         NULL
                    )""";

            String sqlCreateMasterKitty = """
                    CREATE TABLE IF NOT EXISTS master_kitty
                    (
                        master_id INT NOT NULL,
                        kitty_id  INT NOT NULL,
                        PRIMARY KEY (master_id, kitty_id),
                        FOREIGN KEY (master_id) REFERENCES masters (id),
                        FOREIGN KEY (kitty_id) REFERENCES kitties (id)
                    );
                    """;

            statement.execute(sqlCreateTableKitty);
            statement.execute(sqlCreateTableMaster);
            statement.execute(sqlCreateMasterKitty);
        }
    }

    private static void createStreetHouseTables(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            String sqlCreateTableStreet = """
                    CREATE TABLE IF NOT EXISTS streets
                    (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NULL,
                        postcode INT NULL
                    )
                    """;

            String sqlCreateTableHouse = """
                    CREATE TABLE IF NOT EXISTS houses
                    (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NULL ,
                        date_building DATE NULL ,
                        floors INT NULL ,
                        type ENUM('LIVING_QUARTERS','COMMERCIAL','GARAGE','ANCILLARY') DEFAULT 'LIVING_QUARTERS',
                        street_id BIGINT,
                        FOREIGN KEY (street_id) REFERENCES streets (id)
                    )
                    """;
            statement.execute(sqlCreateTableStreet);
            statement.execute(sqlCreateTableHouse);
        }
    }

    private static void createEmployeeTask(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            String sqlCreateTableTask = """
                    CREATE TABLE IF NOT EXISTS tasks
                    (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(64) NULL,
                        deadline DATE NULL,
                        description VARCHAR(256) NULL,
                        type ENUM('NEW_FUNCTIONALITY',
                            'BUG',
                            'IMPROVEMENT',
                            'ANALYTICS') NULL,
                        employee_id BIGINT NULL,
                        FOREIGN KEY (employee_id) REFERENCES  employees(id)
                    );
                    """;

            String sqlCreateTableEmployee = """
                    CREATE TABLE IF NOT EXISTS employees
                    (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(64) NULL ,
                        birth_date DATE NULL
                    );
                    """;
            statement.execute(sqlCreateTableEmployee);
            statement.execute(sqlCreateTableTask);
        }
    }

    private static void createBrandModel(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {

            String sqlCreateTableBrand = """
                    CREATE TABLE IF NOT EXISTS brands_car
                    (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name varchar(64) NULL,
                        date DATE NULL
                    );
                    """;

            String sqlCreateTableModel = """
                    CREATE TABLE IF NOT EXISTS models_car
                    (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        name varchar(64),
                        length INT,
                        width INT,
                        body ENUM('SEDAN', 'HATCHBACK', 'STATION_WAGON', 'COUPE', 'PICKUP', 'ROADSTER') DEFAULT 'COUPE',
                        brand_id BIGINT,
                        FOREIGN KEY (brand_id) REFERENCES brands_car(id)
                    );
                    """;

            statement.execute(sqlCreateTableBrand);
            statement.execute(sqlCreateTableModel);
        }
    }
}
