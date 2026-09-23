package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {

    public List<String> listarNomesInstrutores() {
        List<String> emails = new ArrayList<>();
        String sql = """
                SELECT u.email
                FROM instrutor i
                INNER JOIN usuario u ON u.id_usuario = i.id_usuario
                ORDER BY u.email
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                emails.add(rs.getString("email"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar instrutores", e);
        }
        return emails;
    }

    public void salvarTurma(String nomeTurma, String turno, String emailInstrutor, String statusTurma) {
        String inserirTurma = "INSERT INTO turma (nome_turma, turno, situacao) VALUES (?, ?, ?)";
        String buscarInstrutor = """
                SELECT i.id_instrutor, u.id_usuario
                FROM instrutor i
                INNER JOIN usuario u ON u.id_usuario = i.id_usuario
                WHERE u.email = ?
                LIMIT 1
                """;
        String inserirVinculo = """
                INSERT INTO instrutor_gerencia_turma (id_usuario, id_turma, id_instrutor)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idTurma;
                try (PreparedStatement stmtTurma = conn.prepareStatement(inserirTurma, Statement.RETURN_GENERATED_KEYS)) {
                    stmtTurma.setString(1, nomeTurma);
                    stmtTurma.setString(2, turno);
                    stmtTurma.setString(3, "Em Andamento".equals(statusTurma) ? "A" : "C");
                    stmtTurma.executeUpdate();
                    try (ResultSet rs = stmtTurma.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("Não foi possível obter o id_turma.");
                        }
                        idTurma = rs.getInt(1);
                    }
                }

                int idInstrutor;
                int idUsuario;
                try (PreparedStatement stmtInstrutor = conn.prepareStatement(buscarInstrutor)) {
                    stmtInstrutor.setString(1, emailInstrutor);
                    try (ResultSet rs = stmtInstrutor.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Instrutor não encontrado.");
                        }
                        idInstrutor = rs.getInt("id_instrutor");
                        idUsuario = rs.getInt("id_usuario");
                    }
                }

                try (PreparedStatement stmtVinculo = conn.prepareStatement(inserirVinculo)) {
                    stmtVinculo.setInt(1, idUsuario);
                    stmtVinculo.setInt(2, idTurma);
                    stmtVinculo.setInt(3, idInstrutor);
                    stmtVinculo.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar turma", e);
        }
    }
}
