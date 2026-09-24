package org.githubio.desktop_beleza.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Carrega o arquivo .env da raiz do projeto de forma segura
    private static final Dotenv dotenv = Dotenv.load();

    // Monta a URL e puxa o usuário e senha dinamicamente do .env
    private static final String URL = "jdbc:mysql://" + dotenv.get("DB_HOST") + ":" +
            dotenv.get("DB_PORT") + "/" + dotenv.get("DB_NAME") +
            "?sslMode=REQUIRED" +
            "&requireSSL=true" +
            "&verifyServerCertificate=false";

    private static final String USUARIO = dotenv.get("DB_USER");
    private static final String SENHA = dotenv.get("DB_PASS");

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