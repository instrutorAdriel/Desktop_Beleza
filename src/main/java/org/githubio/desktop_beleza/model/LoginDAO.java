package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDAO {

    /**
     * O Desktop é a área do instrutor. Por isso o login só é aceito quando
     * o usuário do e-mail também possui registro na tabela instrutor.
     */
    public boolean autenticarUsuario(String email, String senha) {
        String sql = """
                SELECT u.senha
                FROM usuario u
                INNER JOIN instrutor i ON i.id_usuario = u.id_usuario
                WHERE u.email = ?
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String senhaHash = rs.getString("senha");
                return senhaHash != null && BCrypt.checkpw(senha, senhaHash);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar usuário", e);
        }
    }
}
