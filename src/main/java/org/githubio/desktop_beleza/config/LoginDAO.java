package org.githubio.desktop_beleza.config;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginDAO {

    public void cadastrarUsuario(String nome_instrutor, String senha) {

        // Comando SQL de inserção.
        // Os três ? serão substituídos pelos valores reais logo abaixo.
        // Usar ? (em vez de concatenar strings) protege contra SQL Injection.
        String sql = "INSERT INTO tb_instrutor (email_instrutor, senha) VALUES (?, ?)";
        String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)

        ) {
            stmt.setString(1, nome_instrutor);

            stmt.setString(2, senhaHash);

            stmt.executeUpdate();

            IO.println("Usuário cadastrado com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean autenticarUsuario(String nome, String senha) {
        String sql = "SELECT senha FROM tb_instrutor WHERE email_instrutor = ?";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, nome);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String senhaHash = rs.getString("senha");

                    // checkpw compara a senha digitada com o hash salvo no banco
                    return BCrypt.checkpw(senha, senhaHash);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar usuário", e);
        }

        return false; // usuário não encontrado
    }

    public boolean instrutorExiste(String email) {
        // 1. A Query busca se existe um instrutor com esse e-mail
        String sql = "SELECT * FROM tb_instrutor WHERE email_instrutor = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt  = conn.prepareStatement(sql);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            // Retorna true se encontrar o registro, false se não
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Erro ao validar e-mail: " + e.getMessage());
            return false;
        }

    }

    public boolean atualizarSenha(String email, String senhaHash) {
        String sql = "UPDATE tb_instrutor SET senha = ? WHERE email_instrutor = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, senhaHash);
            stmt.setString(2, email);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar: " + e.getMessage());
            return false;
        }
    }
        }

