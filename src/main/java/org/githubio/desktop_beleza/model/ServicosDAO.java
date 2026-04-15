package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;
import java.sql.*;
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
                        rs.getString("horario_inicio") + " - " + rs.getString("horario_fim")
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
    // INSERIR (NOVO)
    // =========================
    public void inserir(Servico servico) {
        String sql = "INSERT INTO tb_servicos (nome_servico, descricao, horario_inicio, horario_fim) VALUES (?, ?, ?, ?)";

        // Divide o horário "08:00 - 12:00" em duas partes
        String[] partes = tratarHorario(servico.getHorario());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getDescricao());
            stmt.setString(3, partes[0]); // Início
            stmt.setString(4, partes[1]); // Fim

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir serviço", e);
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
            WHERE id_servico = ?
        """;

        String[] partes = tratarHorario(servico.getHorario());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, servico.getNome());
            stmt.setString(2, servico.getDescricao());
            stmt.setString(3, partes[0]);
            stmt.setString(4, partes[1]);
            stmt.setInt(5, servico.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar serviço", e);
        }
    }

    // =========================
    // EXCLUIR
    // =========================
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

    // Método auxiliar para não repetir código de tratamento de string
    private String[] tratarHorario(String horarioFull) {
        if (horarioFull != null && horarioFull.contains("-")) {
            String[] partes = horarioFull.split("-");
            return new String[]{partes[0].trim(), partes[1].trim()};
        }
        return new String[]{"00:00", "00:00"}; // Fallback caso o formato esteja errado
    }
}