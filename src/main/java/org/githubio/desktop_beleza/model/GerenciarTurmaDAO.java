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
        // 1. Adicionei t.status_turma no SELECT
        String sql = """
    SELECT t.id_turma, t.turma, t.turno, t.status_turma, ti.nome_instrutor
    FROM rl_instrutor_turmas i
    INNER JOIN tb_turmas t ON i.id_turma = t.id_turma
    INNER JOIN tb_instrutor ti ON i.id_instrutor = ti.id_instrutor
    """;

        ObservableList<UsuarioDTO> lista = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // 2. Agora passamos 5 parâmetros para o construtor, incluindo o status!
                lista.add(new UsuarioDTO(
                        rs.getInt("id_turma"),
                        rs.getString("turma"),
                        rs.getString("turno"),
                        rs.getString("nome_instrutor"),
                        rs.getString("status_turma") // PEGA O STATUS AQUI
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

    public void atualizarCompleto(int id, String nome, String turno, String instrutor, String status) {
        // Aqui usamos o nome da coluna do banco: status_turma
        String sql = "UPDATE tb_turmas SET turma = ?, turno = ?, status_turma = ? WHERE id_turma = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);
            stmt.setString(2, turno);
            stmt.setString(3, status);
            stmt.setInt(4, id);

            stmt.executeUpdate();

            // Se você tiver uma tabela de relação para o instrutor,
            // precisará de outro UPDATE ou uma lógica para atualizar o instrutor também.

        } catch (SQLException e) {
            e.printStackTrace();
        }
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
