package org.githubio.desktop_beleza.model.dao;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class PasswordResetTokenDAO {
    public void salvarCodigoOtp(int idUsuario, String codigoOtp) {
        inativarTokensDoUsuario(idUsuario);

        String sql = """
                INSERT INTO passwordresettoken(id_usuario, token, data_expiracao)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setString(2, codigoOtp);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)));

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validarCodigoOtp(int idUsuario, String codigoOtpDigitado) {
        String sql = """
                SELECT COUNT(*)
                FROM passwordresettoken
                WHERE id_usuario = ? AND token = ? AND data_expiracao > ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setString(2, codigoOtpDigitado);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao validar o código OTP.", e);
        }

        return false;
    }

    public void inativarTokensDoUsuario(int idUsuario) {
        String sql = """
                DELETE FROM passwordresettoken WHERE id_usuario = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, idUsuario);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inativar tokens do usuário.", e);
        }
    }
}
