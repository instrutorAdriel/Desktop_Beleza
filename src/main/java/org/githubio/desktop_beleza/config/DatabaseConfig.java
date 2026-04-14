package org.githubio.desktop_beleza.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3307/db_salao_de_beleza";
        String user = "root";
        String password = "senac";

        return DriverManager.getConnection(url, user, password);
    }
}
