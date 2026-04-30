package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AgendaDAO {

    public List<String> listarServicos() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nome_servico FROM tb_servicos";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nome_servico"));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar serviços: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Retorna todas as turmas vinculadas ao instrutor logado.
     * Cada elemento do array: [0] = id_turmas_instrutores, [1] = "Turma X - Turno"
     */
    public List<String[]> listarTurmasDoInstrutor(String emailInstrutor) {
        List<String[]> lista = new ArrayList<>();
        String sql = "SELECT rti.id_turmas_instrutores, t.turma, t.turno " +
                "FROM rl_turmas_instrutores rti " +
                "JOIN tb_turmas t ON rti.id_turma = t.id_turma " +
                "JOIN tb_instrutores ti ON rti.id_instrutor = ti.id_instrutor " +
                "WHERE ti.email_instrutor = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailInstrutor);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id_turmas_instrutores")),
                        rs.getString("turma") + " - " + rs.getString("turno")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar turmas do instrutor: " + e.getMessage());
        }
        return lista;
    }

    public int cadastrarERetornarIdModelo(String nomeCliente) {
        String sqlSelect = "SELECT id_modelo FROM tb_modelos WHERE nome_modelo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setString(1, nomeCliente);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id_modelo");
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

    public int cadastrarERetornarIdServico(String nomeServico) {
        String sql = "SELECT id_servico FROM tb_servicos WHERE nome_servico = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeServico);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("id_servico");
        } catch (SQLException e) {
            System.err.println("Erro ao buscar serviço: " + e.getMessage());
        }
        return -1;
    }

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

    // Todos os agendamentos do instrutor (sem filtro de turma)
    public List<Agenda> listarAgendamentos(String emailInstrutor) {
        List<Agenda> lista = new ArrayList<>();
        String sql = "SELECT a.id_agenda, a.data_agenda, a.horario_agenda, " +
                "s.nome_servico, m.nome_modelo, sa.status_agenda " +
                "FROM tb_agenda a " +
                "JOIN tb_servicos s ON a.id_servico = s.id_servico " +
                "JOIN tb_modelos m ON a.id_modelo = m.id_modelo " +
                "JOIN tb_status_agenda sa ON a.id_status_agenda = sa.id_status_agenda " +
                "JOIN rl_turmas_instrutores rti ON a.id_turmas_instrutores = rti.id_turmas_instrutores " +
                "JOIN tb_instrutores ti ON rti.id_instrutor = ti.id_instrutor " +
                "WHERE ti.email_instrutor = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailInstrutor);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Agenda(
                        rs.getInt("id_agenda"),
                        rs.getString("data_agenda"),
                        rs.getString("horario_agenda"),
                        rs.getString("status_agenda"),
                        rs.getString("nome_modelo"),
                        rs.getString("nome_servico")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar: " + e.getMessage());
        }
        return lista;
    }

    // Todos os agendamentos filtrados por turma específica (usado no "Ver Todos")
    public List<Agenda> listarAgendamentosPorTurma(String emailInstrutor, int idTurmasInstrutores) {
        List<Agenda> lista = new ArrayList<>();
        String sql = "SELECT a.id_agenda, a.data_agenda, a.horario_agenda, " +
                "s.nome_servico, m.nome_modelo, sa.status_agenda " +
                "FROM tb_agenda a " +
                "JOIN tb_servicos s ON a.id_servico = s.id_servico " +
                "JOIN tb_modelos m ON a.id_modelo = m.id_modelo " +
                "JOIN tb_status_agenda sa ON a.id_status_agenda = sa.id_status_agenda " +
                "JOIN rl_turmas_instrutores rti ON a.id_turmas_instrutores = rti.id_turmas_instrutores " +
                "JOIN tb_instrutores ti ON rti.id_instrutor = ti.id_instrutor " +
                "WHERE ti.email_instrutor = ? " +
                "AND a.id_turmas_instrutores = ? " +
                "ORDER BY a.data_agenda, a.horario_agenda";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailInstrutor);
            stmt.setInt(2, idTurmasInstrutores);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Agenda(
                        rs.getInt("id_agenda"),
                        rs.getString("data_agenda"),
                        rs.getString("horario_agenda"),
                        rs.getString("status_agenda"),
                        rs.getString("nome_modelo"),
                        rs.getString("nome_servico")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar por turma: " + e.getMessage());
        }
        return lista;
    }

    // Agendamentos por semana sem filtro de turma (mantido para compatibilidade)
    public List<Agenda> listarAgendamentosPorSemana(LocalDate inicioSemana, LocalDate fimSemana, String emailInstrutor) {
        List<Agenda> lista = new ArrayList<>();
        String sql = "SELECT a.id_agenda, a.data_agenda, a.horario_agenda, " +
                "s.nome_servico, m.nome_modelo, sa.status_agenda " +
                "FROM tb_agenda a " +
                "JOIN tb_servicos s ON a.id_servico = s.id_servico " +
                "JOIN tb_modelos m ON a.id_modelo = m.id_modelo " +
                "JOIN tb_status_agenda sa ON a.id_status_agenda = sa.id_status_agenda " +
                "JOIN rl_turmas_instrutores rti ON a.id_turmas_instrutores = rti.id_turmas_instrutores " +
                "JOIN tb_instrutores ti ON rti.id_instrutor = ti.id_instrutor " +
                "WHERE ti.email_instrutor = ? " +
                "AND a.data_agenda BETWEEN ? AND ? " +
                "ORDER BY a.data_agenda, a.horario_agenda";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailInstrutor);
            stmt.setString(2, inicioSemana.toString());
            stmt.setString(3, fimSemana.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Agenda(
                        rs.getInt("id_agenda"),
                        rs.getString("data_agenda"),
                        rs.getString("horario_agenda"),
                        rs.getString("status_agenda"),
                        rs.getString("nome_modelo"),
                        rs.getString("nome_servico")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar por semana: " + e.getMessage());
        }
        return lista;
    }

    // Agendamentos por semana E por turma específica (visão padrão da tela)
    public List<Agenda> listarAgendamentosPorSemanaETurma(LocalDate inicioSemana, LocalDate fimSemana,
                                                          String emailInstrutor, int idTurmasInstrutores) {
        List<Agenda> lista = new ArrayList<>();
        String sql = "SELECT a.id_agenda, a.data_agenda, a.horario_agenda, " +
                "s.nome_servico, m.nome_modelo, sa.status_agenda " +
                "FROM tb_agenda a " +
                "JOIN tb_servicos s ON a.id_servico = s.id_servico " +
                "JOIN tb_modelos m ON a.id_modelo = m.id_modelo " +
                "JOIN tb_status_agenda sa ON a.id_status_agenda = sa.id_status_agenda " +
                "JOIN rl_turmas_instrutores rti ON a.id_turmas_instrutores = rti.id_turmas_instrutores " +
                "JOIN tb_instrutores ti ON rti.id_instrutor = ti.id_instrutor " +
                "WHERE ti.email_instrutor = ? " +
                "AND a.id_turmas_instrutores = ? " +
                "AND a.data_agenda BETWEEN ? AND ? " +
                "ORDER BY a.data_agenda, a.horario_agenda";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailInstrutor);
            stmt.setInt(2, idTurmasInstrutores);
            stmt.setString(3, inicioSemana.toString());
            stmt.setString(4, fimSemana.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                lista.add(new Agenda(
                        rs.getInt("id_agenda"),
                        rs.getString("data_agenda"),
                        rs.getString("horario_agenda"),
                        rs.getString("status_agenda"),
                        rs.getString("nome_modelo"),
                        rs.getString("nome_servico")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar por semana e turma: " + e.getMessage());
        }
        return lista;
    }

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

    public void editarAgendamento(Agenda agenda) {
        String sql = "UPDATE tb_agenda SET data_agenda = ?, horario_agenda = ? WHERE id_agenda = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, agenda.getData());
            stmt.setString(2, agenda.getHorario());
            stmt.setInt(3, agenda.getId());
            int linhas = stmt.executeUpdate();
            if (linhas > 0) System.out.println("Agendamento " + agenda.getId() + " atualizado!");
        } catch (SQLException e) {
            System.err.println("Erro ao editar: " + e.getMessage());
        }
    }

    public void atualizarStatus(int idAgenda, String novoStatus) {
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