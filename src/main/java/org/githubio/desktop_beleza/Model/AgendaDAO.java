package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AgendaDAO {

    // BUSCA OU CADASTRA CLIENTE
    public int cadastrarERetornarIdModelo(String nomeCliente) {
        String sqlSelect = "SELECT id_modelos FROM tb_modelos WHERE nome_modelo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {

            stmt.setString(1, nomeCliente);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id_modelos");

        } catch (SQLException e) {
            System.err.println("Erro ao buscar cliente: " + e.getMessage());
        }

        String sqlInsert = "INSERT INTO tb_modelos (nome_modelo) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nomeCliente);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar cliente: " + e.getMessage());
        }

        return -1;
    }

    // BUSCA OU CADASTRA SERVIÇO
    public int cadastrarERetornarIdServico(String nomeServico) {
        String sqlSelect = "SELECT id_servico FROM tb_servicos WHERE nome_servico = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {

            stmt.setString(1, nomeServico);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id_servico");

        } catch (SQLException e) {
            System.err.println("Erro ao buscar serviço: " + e.getMessage());
        }

        String sqlInsert = "INSERT INTO tb_servicos (nome_servico) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nomeServico);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar serviço: " + e.getMessage());
        }

        return -1;
    }

    // CADASTRAR AGENDAMENTO
    public void cadastrarAgendamento(String data, String horario,
                                     int idStatusAgenda, int idModelo,
                                     int idServico, int idTurmasInstrutores) {

        String sql = "INSERT INTO tb_agenda " +
                "(data_agenda, horario_agenda, id_status_agenda, " +
                "id_modelo, id_servico, id_turmas_instrutores) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, data);
            stmt.setString(2, horario);
            stmt.setInt(3, idStatusAgenda);
            stmt.setInt(4, idModelo);
            stmt.setInt(5, idServico);
            stmt.setInt(6, idTurmasInstrutores);

            stmt.executeUpdate();
            System.out.println("Agendamento cadastrado com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar: " + e.getMessage());
        }
    }

    // LISTAR
    public List<Agenda> listarAgendamentos() {
        List<Agenda> lista = new ArrayList<>();

        String sql = "SELECT a.id_agenda, a.data_agenda, a.horario_agenda, " +
                "s.nome_servico, m.nome_modelo, sa.status_agenda " +
                "FROM tb_agenda a " +
                "JOIN tb_servicos s ON a.id_servico = s.id_servico " +
                "JOIN tb_modelos m ON a.id_modelo = m.id_modelo " +
                "JOIN tb_status_agenda sa ON a.id_status_agenda = sa.id_status_agenda";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id         = rs.getInt("id_agenda");
                String data    = rs.getString("data_agenda");
                String horario = rs.getString("horario_agenda");
                String servico = rs.getString("nome_servico");
                String cliente = rs.getString("nome_modelo");
                String status  = rs.getString("status_agenda");

                Agenda a = new Agenda(id, data, horario, status, cliente, servico);
                lista.add(a);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar: " + e.getMessage());
        }
        return lista;
    }

    // EXCLUIR
    public void excluirAgendamento(int id) {
        String sql = "DELETE FROM tb_agenda WHERE id_agenda = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Excluído com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao excluir: " + e.getMessage());
        }
    }

    // EDITAR
    public void editarAgendamento(Agenda agenda) {
        String sql = "UPDATE tb_agenda SET data_agenda = ?, horario_agenda = ? " +
                "WHERE id_agenda = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, agenda.getData());
            stmt.setString(2, agenda.getHorario());
            stmt.setInt(3, agenda.getId());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Agendamento " + agenda.getId() + " atualizado!");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao editar: " + e.getMessage());
        }
    }
    public void atualizarStatus(int idAgenda, String novoStatus) {
        // Primeiro busca o id_status_agenda pelo nome
        String sqlSelect = "SELECT id_status_agenda FROM tb_status_agenda WHERE status_agenda = ?";
        int idStatus = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {

            stmt.setString(1, novoStatus);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) idStatus = rs.getInt("id_status_agenda");

        } catch (SQLException e) {
            System.err.println("Erro ao buscar status: " + e.getMessage());
        }

        if (idStatus == -1) return;

        // Depois atualiza a agenda
        String sqlUpdate = "UPDATE tb_agenda SET id_status_agenda = ? WHERE id_agenda = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {

            stmt.setInt(1, idStatus);
            stmt.setInt(2, idAgenda);
            int linhas = stmt.executeUpdate();
            if (linhas > 0) System.out.println("Status atualizado com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status: " + e.getMessage());
        }
    }
}