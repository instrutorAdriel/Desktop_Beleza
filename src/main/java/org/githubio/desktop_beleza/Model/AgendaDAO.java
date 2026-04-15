package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AgendaDAO {
    /*
    Coloque as Classes AgendaDAO, Agenda no pacote org.githubio.desktop_beleza.Model
    E a Classe AgendaController no pacote org.githubio.desktop_beleza.Controller

    Não vou mesclar com a main pois preciso que esse código adaptado com a tela do pessoal do front-end
     */

    // Método de Cadastrar
    public void cadastrarAgendamento(String tipo_pratica, String data_aula, String horario_aula, String status_agenda, String id_cliente, String id_servico, String id_turma) {
        // ATENÇÃO: Verifique se o nome é 'agenda' ou 'agendamentos' para bater com o editar/excluir
        String sql = "INSERT INTO tb_agendas (tipo_pratica, data_aula, horario_aula, status_agenda, id_cliente, id_servico, id_turma) VALUES (?, ?, ?, ?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo_pratica);
            stmt.setString(2, data_aula);
            stmt.setString(3, horario_aula);
            stmt.setString(4, status_agenda);
            stmt.setString(5, id_cliente);
            stmt.setString(6, id_servico);
            stmt.setString(7, id_turma);

            stmt.executeUpdate();
            IO.println("Agendamento cadastrado com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<org.githubio.desktop_beleza.model.Agenda> listarAgendamentos() {
        List<org.githubio.desktop_beleza.model.Agenda> lista = new ArrayList<>();
        String sql = "SELECT * FROM tb_agendas";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_agenda");
                String pratica = rs.getString("tipo_pratica");
                String data = rs.getString("data_aula");
                String horario = rs.getString("horario_aula");
                String status = rs.getString("status_agenda");

                // Verifique se esses IDs não deveriam ser rs.getInt()
                String cliente = rs.getString("id_cliente");
                String servico = rs.getString("id_servico");

                Agenda a = new Agenda(id, pratica, data, horario, status, cliente, servico);

                lista.add(a);
            }
        } catch (SQLException e) {
            // Se a classe IO não existir, use System.err.println
            System.err.println("Erro ao listar: " + e.getMessage());
        }
        return lista;
    }

    // Método de Excluir
    public void excluirAgendamento(int id) {
        String sql = "DELETE FROM tb_agendas WHERE id_agenda = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            IO.println("Excluído com sucesso no banco!");
        } catch (SQLException e) {
            IO.println("Erro ao excluir: " + e.getMessage());
        }
    }

    // Método de Editar
    public void editarAgendamento(Agenda agenda) {
        // 1. Definição do SQL
        String sql = "UPDATE tb_agendas SET tipo_pratica = ?, data_aula = ?, " +
                "horario_aula = ?, id_cliente = ?, id_servico = ? WHERE id_agenda = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 2. Preenchimento na ordem correta
            stmt.setString(1, agenda.getPratica());  // 1º ? -> tipo_pratica
            stmt.setString(2, agenda.getData());     // 2º ? -> data_aula
            stmt.setString(3, agenda.getHorario());  // 3º ? -> horario_aula
            stmt.setString(4, agenda.getCliente());  // 4º ? -> id_cliente
            stmt.setString(5, agenda.getServico());  // 5º ? -> id_servico
            stmt.setInt(6, agenda.getId());          // 6º ? -> id_agenda (O WHERE)

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                IO.println("Agendamento ID " + agenda.getId() + " atualizado com sucesso!");
            }

        } catch (SQLException e) {
            IO.println("Erro ao editar no banco: " + e.getMessage());
        }
    }
}