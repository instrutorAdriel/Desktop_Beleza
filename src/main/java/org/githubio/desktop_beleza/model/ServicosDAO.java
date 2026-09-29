package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServicosDAO {

    public List<Servico> lerTodos() {
        String sql = "SELECT id_produto, nome_produto, descricao FROM produto ORDER BY nome_produto";
        List<Servico> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Servico(
                        rs.getInt("id_produto"),
                        rs.getString("nome_produto"),
                        rs.getString("descricao")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos/serviços", e);
        }
        return lista;
    }

    public void inserir(Servico servico) {
        String sql = "INSERT INTO produto (nome_produto, descricao, imagem_anexo) VALUES (?, ?, NULL)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getDescricao());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir produto/serviço", e);
        }
    }

    public void atualizar(Servico servico) {
        String sql = "UPDATE produto SET nome_produto = ?, descricao = ? WHERE id_produto = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getDescricao());
            stmt.setInt(3, servico.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar produto/serviço", e);
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM produto WHERE id_produto = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir produto/serviço. Verifique os vínculos com unidades e disponibilidades.", e);
        }
    }
}
