package org.githubio.desktop_beleza.model.dao;

import org.githubio.desktop_beleza.config.DatabaseConnection;
import org.githubio.desktop_beleza.model.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServicosDAO {

    public List<Produto> lerTodos() {
        String sql = "SELECT id_produto, nome_produto, descricao FROM produto ORDER BY nome_produto";
        List<Produto> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Produto(
                        rs.getInt("id_produto"),
                        rs.getString("nome_produto"),
                        rs.getString("descricao")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos/procedimentos", e);
        }
        return lista;
    }

    public void inserir(Produto produto) {
        String sql = "INSERT INTO produto (nome_produto, descricao, imagem_anexo) VALUES (?, ?, NULL)";

        // Adicionado Statement.RETURN_GENERATED_KEYS no prepareStatement
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.executeUpdate();

            // Recupera o id_produto auto-incrementado pelo MySQL
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    produto.setId(rs.getInt(1)); // Atribui o ID gerado de volta ao DTO
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir produto/procedimento", e);
        }
    }

    public void atualizar(Produto produto) {
        String sql = "UPDATE produto SET nome_produto = ?, descricao = ? WHERE id_produto = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.setInt(3, produto.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar produto/procedimento", e);
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM produto WHERE id_produto = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir produto/procedimento. Verifique os vínculos com unidades e disponibilidades.", e);
        }
    }
}
