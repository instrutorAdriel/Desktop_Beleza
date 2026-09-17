package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CadastroDAO {

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

    public void cadastrarUsuario(String nome, String email, String senha) {
        String buscarUsuario = "SELECT id_usuario FROM usuario WHERE email = ? ORDER BY id_usuario LIMIT 1";
        String inserirUsuario = "INSERT INTO usuario (email, nome_usuario, senha) VALUES (?, ?, ?)";
        String atualizarUsuario = "UPDATE usuario SET nome_usuario = ?, senha = ? WHERE id_usuario = ?";
        String inserirInstrutor = "INSERT INTO instrutor (id_usuario) VALUES (?)";
        String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Integer idUsuario = null;
                try (PreparedStatement stmtBusca = conn.prepareStatement(buscarUsuario)) {
                    stmtBusca.setString(1, email);
                    try (ResultSet rs = stmtBusca.executeQuery()) {
                        if (rs.next()) idUsuario = rs.getInt("id_usuario");
                    }
                }

                if (idUsuario == null) {
                    try (PreparedStatement stmtUsuario = conn.prepareStatement(inserirUsuario, Statement.RETURN_GENERATED_KEYS)) {
                        stmtUsuario.setString(1, email);
                        stmtUsuario.setString(2, nome);
                        stmtUsuario.setString(3, senhaHash);
                        stmtUsuario.executeUpdate();
                        try (ResultSet rs = stmtUsuario.getGeneratedKeys()) {
                            if (!rs.next()) throw new SQLException("Não foi possível obter o id_usuario gerado.");
                            idUsuario = rs.getInt(1);
                        }
                    }
                } else {
                    try (PreparedStatement stmtAtualiza = conn.prepareStatement(atualizarUsuario)) {
                        stmtAtualiza.setString(1, nome);
                        stmtAtualiza.setString(2, senhaHash);
                        stmtAtualiza.setInt(3, idUsuario);
                        stmtAtualiza.executeUpdate();
                    }
                }

                try (PreparedStatement stmtInstrutor = conn.prepareStatement(inserirInstrutor)) {
                    stmtInstrutor.setInt(1, idUsuario);
                    stmtInstrutor.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar instrutor", e);
        }
    }
}
