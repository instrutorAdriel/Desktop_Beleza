package org.githubio.desktop_beleza.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Banco informado para o projeto. Mantida a porta 3307 que já estava no Desktop original.
    private static final String URL = "jdbc:mysql://localhost:3307/beleza_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo";
    private static final String USUARIO = "root";
    private static final String SENHA = "senac";

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar com o banco beleza_db: " + e.getMessage());
            throw new RuntimeException("Não foi possível estabelecer conexão com o banco de dados.", e);
        }
    }
}
