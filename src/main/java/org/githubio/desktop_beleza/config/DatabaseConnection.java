package org.githubio.desktop_beleza.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Carrega o .env ignorando erro se ele não existir
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    // Lê a URL completa e os dados de login definidos no seu .env
    private static final String URL = dotenv.get("URL");
    private static final String USUARIO = dotenv.get("USUARIO");
    private static final String SENHA = dotenv.get("SENHA");

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        try {
            // Valida se as variáveis do .env foram carregadas antes de tentar conectar
            if (URL == null || USUARIO == null || SENHA == null) {
                throw new IllegalStateException("Variáveis de ambiente (URL, USUARIO, SENHA) não foram encontradas no arquivo .env.");
            }

            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar com o banco de dados: " + e.getMessage());
            throw new RuntimeException("Não foi possível estabelecer conexão com o banco de dados.", e);
        }
    }
}