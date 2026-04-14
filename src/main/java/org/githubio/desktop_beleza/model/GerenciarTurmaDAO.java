package org.githubio.desktop_beleza.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.githubio.desktop_beleza.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class GerenciarTurmaDAO {

    public ObservableList<UsuarioDTO> lerUsuariosParaTabela() {
        // Adicionei o t.id_turma no SELECT
        String sql = """
        SELECT t.id_turma, t.turma, t.turno, ti.nome_instrutor
        FROM rl_instrutor_turmas i
        INNER JOIN tb_turmas t ON i.id_turma = t.id_turma
        INNER JOIN tb_instrutor ti ON i.id_instrutor = ti.id_instrutor
        """;

        ObservableList<UsuarioDTO> lista = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new UsuarioDTO(
                        rs.getInt("id_turma"), // PEGA O ID AQUI
                        rs.getString("turma"),
                        rs.getString("turno"),
                        rs.getString("nome_instrutor")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }



    public void excluirTurma(String nomeTurma) {
        // 1. SQL para apagar o vínculo na tabela de relação
        // Note que usamos um subquery para achar o ID da turma pelo nome
        String sqlVinculo = "DELETE FROM rl_instrutor_turmas WHERE id_turma = (SELECT id_turma FROM tb_turmas WHERE turma = ?)";

        // 2. SQL para apagar a turma de fato
        String sqlTurma = "DELETE FROM tb_turmas WHERE turma = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Desativa o auto-save para controlar o processo manualmente
            conn.setAutoCommit(false);

            try (PreparedStatement stmtVinculo = conn.prepareStatement(sqlVinculo);
                 PreparedStatement stmtTurma = conn.prepareStatement(sqlTurma)) {

                // Executa o primeiro: apaga o vínculo
                stmtVinculo.setString(1, nomeTurma);
                stmtVinculo.executeUpdate();

                // Executa o segundo: apaga a turma
                stmtTurma.setString(1, nomeTurma);
                stmtTurma.executeUpdate();

                // Se chegou aqui sem erro, salva as mudanças permanentemente
                conn.commit();
                System.out.println("Vínculo e turma excluídos com sucesso!");

            } catch (SQLException e) {
                // Se algo deu errado, desfaz tudo (rollback) para não corromper os dados
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizarCompleto(int idTurma, String novoNome, String novoTurno, String novoInstrutor) {
        String sqlTurma = "UPDATE tb_turmas SET turma = ?, turno = ? WHERE id_turma = ?";
        String sqlRelacao = """
        UPDATE rl_instrutor_turmas 
        SET id_instrutor = (SELECT id_instrutor FROM tb_instrutor WHERE nome_instrutor = ? LIMIT 1)
        WHERE id_turma = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt1 = conn.prepareStatement(sqlTurma);
                 PreparedStatement stmt2 = conn.prepareStatement(sqlRelacao)) {

                stmt1.setString(1, novoNome);
                stmt1.setString(2, novoTurno);
                stmt1.setInt(3, idTurma); // Atualiza pelo ID (Seguro!)
                stmt1.executeUpdate();

                stmt2.setString(1, novoInstrutor);
                stmt2.setInt(2, idTurma); // Vincula ao ID da turma
                stmt2.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<String> listarNomesInstrutores() {
        List<String> nomes = new ArrayList<>();
        String sql = "SELECT nome_instrutor FROM tb_instrutor ORDER BY nome_instrutor";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                nomes.add(rs.getString("nome_instrutor"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nomes;
    }




}
