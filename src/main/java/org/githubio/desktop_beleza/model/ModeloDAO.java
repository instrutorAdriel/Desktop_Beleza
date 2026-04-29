package org.githubio.desktop_beleza.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.githubio.desktop_beleza.config.DatabaseConnection;

public class ModeloDAO {

    public void cadastrar(Modelo m) {
        String sql = "INSERT INTO tb_modelos (nome_modelo, email, telefone) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getNome());
            stmt.setString(2, m.getEmail());
            stmt.setString(3, m.getTelefone());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar: " + e.getMessage());
        }
    }

    public List<Modelo> lerTodos() {
        String sql = "SELECT * FROM tb_modelos";
        List<Modelo> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Cria o objeto com os dados de texto
                Modelo m = new Modelo(
                        rs.getString("nome_modelo"),
                        rs.getString("telefone"),
                        rs.getString("email")
                );

                // Define o ID separadamente como você solicitou
                m.setId(rs.getInt("id_modelo"));

                lista.add(m);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar modelos", e);
        }
        return lista;
    }

    public void atualizar(Modelo m) {
        String sql = "UPDATE tb_modelos SET nome_modelo=?, email=?, telefone=? WHERE id_modelo=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getNome());
            stmt.setString(2, m.getEmail());
            stmt.setString(3, m.getTelefone());
            stmt.setInt(4, m.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar: " + e.getMessage());
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM tb_modelos WHERE id_modelo=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir: " + e.getMessage());
        }
    }
}