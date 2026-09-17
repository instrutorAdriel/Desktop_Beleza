package org.githubio.desktop_beleza.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GerenciarTurmaDAO {

    public ObservableList<UsuarioDTO> lerUsuariosParaTabela(String emailInstrutor) throws SQLException {
        String sql = """
                SELECT t.id_turma,
                       t.nome_turma,
                       t.torma,
                       u.email,
                       CASE WHEN t.situacao = 'A' THEN 'Em Andamento' ELSE 'Finalizada' END AS status_turma
                FROM instrutor_gerencia_turma igt
                INNER JOIN turma t ON t.id_turma = igt.id_turma
                INNER JOIN instrutor i ON i.id_instrutor = igt.id_instrutor
                INNER JOIN usuario u ON u.id_usuario = i.id_usuario
                WHERE u.email = ?
                ORDER BY t.nome_turma
                """;
        return executarConsulta(sql, emailInstrutor, null);
    }

    public ObservableList<UsuarioDTO> buscarTurmaPorNome(String turma) throws SQLException {
        String sql = """
                SELECT t.id_turma,
                       t.nome_turma,
                       t.torma,
                       u.email,
                       CASE WHEN t.situacao = 'A' THEN 'Em Andamento' ELSE 'Finalizada' END AS status_turma
                FROM instrutor_gerencia_turma igt
                INNER JOIN turma t ON t.id_turma = igt.id_turma
                INNER JOIN instrutor i ON i.id_instrutor = igt.id_instrutor
                INNER JOIN usuario u ON u.id_usuario = i.id_usuario
                WHERE u.email = ? AND t.nome_turma = ?
                ORDER BY t.nome_turma
                """;
        return executarConsulta(sql, MainApplication.getUsuario(), turma);
    }

    private ObservableList<UsuarioDTO> executarConsulta(String sql, String email, String nomeTurma) throws SQLException {
        ObservableList<UsuarioDTO> lista = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            if (nomeTurma != null) {
                stmt.setString(2, nomeTurma);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new UsuarioDTO(
                            rs.getInt("id_turma"),
                            rs.getString("nome_turma"),
                            rs.getString("torma"),
                            rs.getString("email"),
                            rs.getString("status_turma")
                    ));
                }
            }
        }
        return lista;
    }

    public void excluirTurma(String nomeTurma) {
        String buscarId = "SELECT id_turma FROM turma WHERE nome_turma = ?";
        String excluirVinculo = "DELETE FROM instrutor_gerencia_turma WHERE id_turma = ?";
        String excluirTurma = "DELETE FROM turma WHERE id_turma = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idTurma;
                try (PreparedStatement stmtBusca = conn.prepareStatement(buscarId)) {
                    stmtBusca.setString(1, nomeTurma);
                    try (ResultSet rs = stmtBusca.executeQuery()) {
                        if (!rs.next()) {
                            return;
                        }
                        idTurma = rs.getInt("id_turma");
                    }
                }

                try (PreparedStatement stmtV = conn.prepareStatement(excluirVinculo);
                     PreparedStatement stmtT = conn.prepareStatement(excluirTurma)) {
                    stmtV.setInt(1, idTurma);
                    stmtV.executeUpdate();
                    stmtT.setInt(1, idTurma);
                    stmtT.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir turma", e);
        }
    }

    public void atualizarCompleto(int idTurma, String nome, String turno, String emailInstrutor, String status) {
        String atualizarTurma = "UPDATE turma SET nome_turma = ?, torma = ?, situacao = ? WHERE id_turma = ?";
        String buscarInstrutor = """
                SELECT i.id_instrutor, u.id_usuario
                FROM instrutor i
                INNER JOIN usuario u ON u.id_usuario = i.id_usuario
                WHERE u.email = ?
                LIMIT 1
                """;
        String atualizarVinculo = """
                UPDATE instrutor_gerencia_turma
                SET id_instrutor = ?, id_usuario = ?
                WHERE id_turma = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idInstrutor;
                int idUsuario;
                try (PreparedStatement stmtBusca = conn.prepareStatement(buscarInstrutor)) {
                    stmtBusca.setString(1, emailInstrutor);
                    try (ResultSet rs = stmtBusca.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Instrutor não encontrado.");
                        }
                        idInstrutor = rs.getInt("id_instrutor");
                        idUsuario = rs.getInt("id_usuario");
                    }
                }

                try (PreparedStatement stmtT = conn.prepareStatement(atualizarTurma);
                     PreparedStatement stmtV = conn.prepareStatement(atualizarVinculo)) {
                    stmtT.setString(1, nome);
                    stmtT.setString(2, turno);
                    stmtT.setString(3, "Em Andamento".equals(status) ? "A" : "C");
                    stmtT.setInt(4, idTurma);
                    stmtT.executeUpdate();

                    stmtV.setInt(1, idInstrutor);
                    stmtV.setInt(2, idUsuario);
                    stmtV.setInt(3, idTurma);
                    stmtV.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar turma", e);
        }
    }

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
}
