package org.githubio.desktop_beleza;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


    public class DataBaseConfig{
        static String url = "jdbc:mysql://localhost:3307/db_salao_de_beleza";
        static String user = "root";
        static String password = "senac";
        public static Connection getConnection() {

            try {
                return DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                throw new RuntimeException("falha de conexão", e);
            }

        }
    }

