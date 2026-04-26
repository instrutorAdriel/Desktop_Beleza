package org.githubio.desktop_beleza.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/db_salao_beleza";
    private static final String USUARIO = "root";
    private static final String SENHA = "752363";

    /**
     * Estabelece uma conexão com o banco de dados.
     * @return Connection objeto de conexão
     * @throws SQLException Caso ocorra um erro na tentativa de conexão
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            IO.println("Erro ao conectar com o banco do Salão de Beleza: " + e.getMessage());
            throw new RuntimeException("Não foi possível estabelecer conexão com o banco de dados.", e);
        }
    }
}
