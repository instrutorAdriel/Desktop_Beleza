package org.githubio.desktop_beleza.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.githubio.desktop_beleza.model.AtualizarSenhaDAO;
import org.githubio.desktop_beleza.config.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

public class AtualizarSenhaDAO {
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

    public boolean atualizarSenha(String email, String senhaHash) {
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

