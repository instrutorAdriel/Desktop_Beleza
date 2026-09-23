package org.githubio.desktop_beleza.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {
    // Conexão com o banco MySQL hospedado no Aiven (nuvem).
    // Se precisar trocar de host/provedor no futuro, só alterar a URL, USUARIO e SENHA abaixo —
    // o resto do projeto (DAOs, etc.) não precisa mudar, pois todos usam getConnection() daqui.
    private static final String URL = "jdbc:mysql://mysql-12b0ec52-alexandresousajoao38-454f.f.aivencloud.com:11325/beleza_db"
            + "?sslMode=REQUIRED"
            + "&requireSSL=true"
            + "&verifyServerCertificate=false";
    private static final String USUARIO = "avnadmin";
    private static final String SENHA ="";

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
