package org.githubio.desktop_beleza.Model;

import org.githubio.desktop_beleza.DatabaseConfig;

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
    public void cadastrarAgendamento(String data_procedimento, String tipo_servico, String tipo_Pratica, String cliente) {
        // ATENÇÃO: Verifique se o nome é 'agenda' ou 'agendamentos' para bater com o editar/excluir
        String sql = "INSERT INTO agenda (data_procedimento, tipo_servico, tipo_Pratica, cliente) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, data_procedimento);
            stmt.setString(2, tipo_servico);
            stmt.setString(3, tipo_Pratica);
            stmt.setString(4, cliente);

            stmt.executeUpdate();
            IO.println("Agendamento cadastrado com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método de Listar
    public List<Agenda> listarAgendamentos() {
        List<Agenda> lista = new ArrayList<>();
        String sql = "SELECT * FROM agenda";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // 1. Pegamos os dados das colunas primeiro
                int id = rs.getInt("id");
                String data = rs.getString("data_procedimento");
                String servico = rs.getString("tipo_servico");
                String pratica = rs.getString("tipo_Pratica");
                String cliente = rs.getString("cliente");
                String status = "Agendado";

                Agenda a = new Agenda(id, data, servico, pratica, cliente, status);

                lista.add(a);
            }
        } catch (SQLException e) {
            IO.println("Erro ao listar: " + e.getMessage());
        }
        return lista;
    }

    // Método de Excluir
    public void excluirAgendamento(int id) {
        String sql = "DELETE FROM agenda WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
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
        String sql = "UPDATE agenda SET data_procedimento = ?, tipo_servico = ?, " +
                "tipo_Pratica = ?, cliente = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, agenda.getData());
            stmt.setString(2, agenda.getServico());
            stmt.setString(3, agenda.getPratica());
            stmt.setString(4, agenda.getCliente());
            stmt.setInt(5, agenda.getId());

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                IO.println("Agendamento ID " + agenda.getId() + " atualizado com sucesso!");
            }

        } catch (SQLException e) {
            IO.println("Erro ao editar no banco: " + e.getMessage());
        }
    }
}