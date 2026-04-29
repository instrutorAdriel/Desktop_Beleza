package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicosDAO {

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
                        rs.getString("horario_disponivel")
                );
                s.setId(rs.getInt("id_servico"));
                lista.add(s);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar serviços", e);
        }
        return lista;
    }

    public void inserir(Servico servico) {
        String sql = """
            INSERT INTO tb_servicos 
            (nome_servico, descricao, horario_disponivel)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getDescricao());
            stmt.setString(3, servico.getHorario());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir serviço", e);
        }
    }

    public void atualizar(Servico servico) {
        String sql = """
            UPDATE tb_servicos
            SET nome_servico = ?,
                descricao = ?,
                horario_disponivel = ?
            WHERE id_servico = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getDescricao());
            stmt.setString(3, servico.getHorario());
            stmt.setInt(4, servico.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar serviço", e);
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM tb_servicos WHERE id_servico = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir serviço", e);
        }
    }
}