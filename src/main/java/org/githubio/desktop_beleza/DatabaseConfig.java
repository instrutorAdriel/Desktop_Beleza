package org.githubio.desktop_beleza;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    public static Connection getConnection () throws SQLException {
        String url = "jdbc:mysql://localhost:3307/BancoAgendamento";
        String user = "root";
        String password = "senac";

        return DriverManager.getConnection(url, user, password);
    }
}
