package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AtualizarSenhaDAO {

    public boolean instrutorExiste(String email) {
        String sql = """
                SELECT 1
                FROM usuario u
                INNER JOIN instrutor i ON i.id_usuario = u.id_usuario
                WHERE u.email = ?
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao validar e-mail", e);
        }
    }

    public boolean atualizarSenha(String email, String senhaHash) {
        String sql = """
                UPDATE usuario u
                INNER JOIN instrutor i ON i.id_usuario = u.id_usuario
                SET u.senha = ?
                WHERE u.email = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, senhaHash);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar senha", e);
        }
    }
}
