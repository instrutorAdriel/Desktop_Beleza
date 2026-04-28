package org.githubio.desktop_beleza.model;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.githubio.desktop_beleza.config.DatabaseConnection;

public class ModeloDAO {

    // INSERT (Usado indiretamente no seu abrirTelaAdcionarPopUp se for modelo novo)
    public void cadastrar(Modelo m) {
        String sql = "INSERT INTO tb_modelos (nome_modelo, email, telefone) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getNome());
            stmt.setString(2, m.getEmail());
            stmt.setString(3, m.getTelefone());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // SELECT (Chamado no atualizarTabela)
    public List<Modelo> lerTodos() {
        List<Modelo> modelos = new ArrayList<>();
        String sql = "SELECT * FROM tb_modelos";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                modelos.add(new Modelo(
                        rs.getInt("id_modelo"),
                        rs.getString("nome_modelo"),
                        rs.getString("telefone"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return modelos;
    }

    // UPDATE (Chamado no abrirDialogoEdicao)
    public void atualizar(Modelo m) {
        String sql = "UPDATE tb_modelos SET nome_modelo=?, email=?, telefone=? WHERE id_modelo=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getNome());
            stmt.setString(2, m.getEmail());
            stmt.setString(3, m.getTelefone());
            stmt.setInt(4, m.getId());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // DELETE (Chamado no confirmarExclusao)
    public void excluir(int id) {
        String sql = "DELETE FROM tb_modelos WHERE id_modelo=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}