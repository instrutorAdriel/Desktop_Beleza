package org.githubio.desktop_beleza.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.githubio.desktop_beleza.config.DatabaseConnection;

public class ModeloDAO {
    public void cadastrarModelo(String nome, String telefone, String email) {

        String sql = "INSERT INTO tb_clientes (nome_cliente, email_cliente, telefone) VALUES (?, ?, ?)";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, nome);
            stmt.setString(2, email);
            stmt.setString(3, telefone);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
