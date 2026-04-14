package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TurmaDAO {

    // Para preencher o seu select na tela
    public List<String> listarNomesInstrutores() {
        List<String> nomes = new ArrayList<>();
        String sql = "SELECT nome_instrutor FROM tb_instrutor";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) nomes.add(rs.getString("nome_instrutor"));
        } catch (SQLException e) { e.printStackTrace(); }
        return nomes;
    }

    public void salvarTurma(String nomeTurma, String turno, String nomeInstrutor) {
        String sqlTurma = "INSERT INTO tb_turmas (turma, turno) VALUES (?, ?)";
        String sqlVinculo = "INSERT INTO rl_instrutor_turmas (id_instrutor, id_turma) VALUES " +
                "((SELECT id_instrutor FROM tb_instrutor WHERE nome_instrutor = ?), ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); // Transação para garantir que os dois inserts funcionem juntos

            // 1. Salva a Turma
            PreparedStatement stmtT = conn.prepareStatement(sqlTurma, Statement.RETURN_GENERATED_KEYS);
            stmtT.setString(1, nomeTurma);
            stmtT.setString(2, turno);
            stmtT.executeUpdate();

            // Pega o ID da turma que acabou de ser gerado
            ResultSet rs = stmtT.getGeneratedKeys();
            if (rs.next()) {
                int idTurma = rs.getInt(1);

                // 2. Cria o vínculo na tabela intermediária
                PreparedStatement stmtV = conn.prepareStatement(sqlVinculo);
                stmtV.setString(1, nomeInstrutor);
                stmtV.setInt(2, idTurma);
                stmtV.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
