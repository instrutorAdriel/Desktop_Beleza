package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServicosDAO {

    // =========================
    // LISTAR TODOS
    // =========================
    public List<Servico> lerTodos() {
        String sql = "SELECT * FROM tb_servicos";
        List<Servico> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Servico s = new Servico(
                        rs.getString("nome_servico"),
                        rs.getString("descricao"),
                        rs.getString("horario_inicio") + " - " +
                                rs.getString("horario_fim")
                );
                s.setId(rs.getInt("id_servico"));
                lista.add(s);
            }

            return lista;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao ler banco", e);
        }
    }

    // =========================
    // ATUALIZAR
    // =========================
    public void atualizar(Servico servico) {
        String sql = """
            UPDATE tb_servicos
            SET nome_servico = ?,
                descricao = ?,
                horario_inicio = ?,
                horario_fim = ?
            WHERE id_servicos = ?
        """;

        // quebra "08:00 - 12:00"
        String[] horario = servico.getHorario().split("-");

        String inicio = horario[0].trim();
        String fim = horario[1].trim();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getDescricao());
            stmt.setString(3, inicio);
            stmt.setString(4, fim);
            stmt.setInt(5, servico.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar serviço", e);
        }
    }
}