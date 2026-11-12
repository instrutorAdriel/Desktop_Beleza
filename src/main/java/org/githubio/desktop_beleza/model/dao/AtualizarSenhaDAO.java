package org.githubio.desktop_beleza.model.dao;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AtualizarSenhaDAO {

    public boolean atualizarSenha(int idUsuario, String senhaHash) {
        String sql = """
                UPDATE usuario
                SET senha = ?
                WHERE id_usuario = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, senhaHash);
            stmt.setInt(2, idUsuario);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar senha", e);
        }
    }

    public Integer buscarIdUsuarioPorEmail(String email) {
        String sql = """
                SELECT u.id_usuario
                FROM usuario u
                WHERE LOWER(TRIM(u.email)) = LOWER(TRIM(?))
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_usuario");
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar ID do usuário pelo e-mail", e);
        }
    }
}
