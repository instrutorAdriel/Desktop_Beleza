package org.githubio.desktop_beleza.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.githubio.desktop_beleza.config.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

public class LoginDAO {
    // Correção: Metodo lerUsuário é inutil no sistema.

    public List<String> lerUsuario(String user) {
        String sql = "SELECT email_instrutor, senha FROM tb_instrutores WHERE nome_cliente = ?";
        List<String> usuarios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String info = rs.getString("nome_instrutor") + " - " + rs.getString("senha");
                    usuarios.add(info);

                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário", e);
        }
        return usuarios;
    }

    public void cadastrarUsuario(String nome_instrutor, String senha) {
        String sql = "INSERT INTO tb_instrutores (email_instrutor, senha) VALUES (?, ?)";
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
        String sql = "SELECT senha FROM tb_instrutores WHERE email_instrutor = ?";

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
        String sql = "SELECT * FROM tb_instrutores WHERE email_instrutor = ?";

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

    public boolean updatePassword(String email, String senhaHash) {
        String sql = "UPDATE tb_instrutores SET senha = ? WHERE email_instrutor = ?";

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

