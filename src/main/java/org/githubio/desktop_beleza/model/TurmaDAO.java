package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {

    public List<String> listarNomesInstrutores() {
        List<String> nomes = new ArrayList<>();
        String sql = "SELECT nome_instrutor FROM tb_instrutores";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) nomes.add(rs.getString("nome_instrutor"));
        } catch (SQLException e) { e.printStackTrace(); }
        return nomes;
    }

    // Adicionamos o parâmetro statusTurma aqui
    public void salvarTurma(String nomeTurma, String turno, String nomeInstrutor, String statusTurma) {
        // 1. Atualizamos o SQL para incluir status_turma
        String sqlTurma = "INSERT INTO tb_turmas (turma, turno, status_turma) VALUES (?, ?, ?)";

        String sqlVinculo = "INSERT INTO rl_turmas_instrutores (id_instrutor, id_turma) VALUES " +
                "((SELECT id_instrutor FROM tb_instrutores WHERE nome_instrutor = ?), ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 2. Preparamos o Insert da Turma com os 3 valores
            PreparedStatement stmtT = conn.prepareStatement(sqlTurma, Statement.RETURN_GENERATED_KEYS);
            stmtT.setString(1, nomeTurma);
            stmtT.setString(2, turno);
            stmtT.setString(3, statusTurma); // Novo campo!
            stmtT.executeUpdate();

            ResultSet rs = stmtT.getGeneratedKeys();
            if (rs.next()) {
                int idTurma = rs.getInt(1);

                PreparedStatement stmtV = conn.prepareStatement(sqlVinculo);
                stmtV.setString(1, nomeInstrutor);
                stmtV.setInt(2, idTurma);
                stmtV.executeUpdate();
            }

            conn.commit();
            System.out.println("Turma salva com sucesso!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}