package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AgendaDAO {

    public List<String> listarServicos() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nome_produto FROM produto ORDER BY nome_produto";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(rs.getString("nome_produto"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar serviços", e);
        }
        return lista;
    }

    public List<String> listarModelos() {
        List<String> lista = new ArrayList<>();
        String sql = """
                SELECT u.nome_usuario
                FROM modelo m
                INNER JOIN usuario u ON u.id_usuario = m.id_usuario
                ORDER BY u.nome_usuario
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(rs.getString("nome_usuario"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar modelos", e);
        }
        return lista;
    }

    public List<String> listarUnidades() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nome_unidade FROM unidade WHERE situacao = 'A' ORDER BY nome_unidade";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(rs.getString("nome_unidade"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar unidades", e);
        }
        return lista;
    }

    public void cadastrarAgendamento(String data, String horario, String nomeModelo,
                                     String nomeServico, String nomeUnidade, String emailInstrutor) {
        String inserirAgendamento = """
            INSERT INTO agendamento
                (data_hora_agendamento, situacao, observacao, id_disponibilidade,
                 id_modelo, id_usuario, id_instrutor)
            VALUES (?, 'Confirmado', NULL, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int[] instrutor = buscarInstrutor(conn, emailInstrutor);
                int idModelo = buscarIdModelo(conn, nomeModelo);
                int idProduto = buscarIdProduto(conn, nomeServico);
                int idUnidade = buscarIdUnidade(conn, nomeUnidade);
                int idProdutoUnidade = buscarOuCriarProdutoUnidade(conn, idUnidade, idProduto);

                LocalDate localDate = LocalDate.parse(data);
                LocalTime localTime = LocalTime.parse(horario);
                int idDisponibilidade = buscarOuCriarDisponibilidade(conn, localDate, localTime, idProdutoUnidade);

                try (PreparedStatement stmt = conn.prepareStatement(inserirAgendamento)) {
                    stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.of(localDate, localTime)));
                    stmt.setInt(2, idDisponibilidade);
                    stmt.setInt(3, idModelo);
                    stmt.setInt(4, instrutor[0]);
                    stmt.setInt(5, instrutor[1]);
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw new SQLException("Erro ao montar o agendamento", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao cadastrar agendamento" + e.getMessage(), e);
        }
    }

    public List<Agenda> listarAgendamentos(String emailInstrutor) {
        String sql = consultaBase() + " ORDER BY a.data_hora_agendamento";
        return executarLista(sql, null, null);
    }

    public List<Agenda> listarAgendamentosPorSemana(LocalDate inicio, LocalDate fim, String emailInstrutor) {
        String sql = consultaBase() + """
             WHERE DATE(a.data_hora_agendamento) BETWEEN ? AND ?
             ORDER BY a.data_hora_agendamento
            """;
        return executarLista(sql, inicio, fim);
    }

    private String consultaBase() {
        return """
            SELECT a.id_agendamento,
                   DATE(a.data_hora_agendamento) AS data_agendamento,
                   TIME_FORMAT(TIME(a.data_hora_agendamento), '%H:%i') AS horario_agendamento,
                   a.situacao,
                   COALESCE(um.nome_usuario, '-') AS nome_modelo,
                   COALESCE(p.nome_produto, '-') AS nome_servico
            FROM agendamento a
            LEFT JOIN modelo m ON m.id_modelo = a.id_modelo
            LEFT JOIN usuario um ON um.id_usuario = m.id_usuario
            LEFT JOIN disponibilidade d ON d.id_disponibilidade = a.id_disponibilidade
            LEFT JOIN produto_unidade pu ON pu.id_produto_unidade = d.id_produto_unidade
            LEFT JOIN produto p ON p.id_produto = pu.id_produto
            """;
    }

    private List<Agenda> executarLista(String sql, LocalDate inicio, LocalDate fim) {
        List<Agenda> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (inicio != null && fim != null) {
                stmt.setDate(1, Date.valueOf(inicio));
                stmt.setDate(2, Date.valueOf(fim));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Agenda(
                            rs.getInt("id_agendamento"),
                            rs.getString("data_agendamento"),
                            rs.getString("horario_agendamento"),
                            rs.getString("situacao"),
                            rs.getString("nome_modelo"),
                            rs.getString("nome_servico")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar agendamentos", e);
        }
        return lista;
    }
    public void excluirAgendamento(int idAgendamento) {
        String sql = "DELETE FROM agendamento WHERE id_agendamento = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAgendamento);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir agendamento", e);
        }
    }

    public void editarAgendamento(Agenda agenda) {
        String buscarContexto = """
                SELECT pu.id_unidade
                FROM agendamento a
                INNER JOIN disponibilidade d ON d.id_disponibilidade = a.id_disponibilidade
                INNER JOIN produto_unidade pu ON pu.id_produto_unidade = d.id_produto_unidade
                WHERE a.id_agendamento = ?
                """;
        String atualizar = """
                UPDATE agendamento
                SET data_hora_agendamento = ?, id_disponibilidade = ?, id_modelo = ?
                WHERE id_agendamento = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idUnidade;
                try (PreparedStatement stmtContexto = conn.prepareStatement(buscarContexto)) {
                    stmtContexto.setInt(1, agenda.getId());
                    try (ResultSet rs = stmtContexto.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Agendamento não encontrado.");
                        }
                        idUnidade = rs.getInt("id_unidade");
                    }
                }

                int idModelo = buscarIdModelo(conn, agenda.getModelo());
                int idProduto = buscarIdProduto(conn, agenda.getServico());
                int idProdutoUnidade = buscarOuCriarProdutoUnidade(conn, idUnidade, idProduto);
                LocalDate data = LocalDate.parse(agenda.getData());
                LocalTime horario = LocalTime.parse(agenda.getHorario());
                int idDisponibilidade = buscarOuCriarDisponibilidade(conn, data, horario, idProdutoUnidade);

                try (PreparedStatement stmt = conn.prepareStatement(atualizar)) {
                    stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.of(data, horario)));
                    stmt.setInt(2, idDisponibilidade);
                    stmt.setInt(3, idModelo);
                    stmt.setInt(4, agenda.getId());
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw new SQLException("Erro ao editar agendamento", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao editar agendamento", e);
        }
    }

    public void atualizarStatus(int idAgendamento, String novoStatus) {
        String sql = "UPDATE agendamento SET situacao = ? WHERE id_agendamento = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus);
            stmt.setInt(2, idAgendamento);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status", e);
        }
    }

    private int[] buscarInstrutor(Connection conn, String email) throws SQLException {
        String sql = """
            SELECT u.id_usuario, i.id_instrutor
            FROM usuario u
            INNER JOIN instrutor i ON i.id_usuario = u.id_usuario
            WHERE u.email = ?
            LIMIT 1
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return new int[]{rs.getInt("id_usuario"), rs.getInt("id_instrutor")};
            }
        }
        throw new SQLException("Instrutor logado não encontrado no banco.");
    }
    private int buscarIdModelo(Connection conn, String nomeModelo) throws SQLException {
        String sql = """
                SELECT m.id_modelo
                FROM modelo m
                INNER JOIN usuario u ON u.id_usuario = m.id_usuario
                WHERE u.nome_usuario = ?
                ORDER BY m.id_modelo
                LIMIT 1
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeModelo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_modelo");
            }
        }
        throw new SQLException("Modelo não encontrado: " + nomeModelo);
    }

    private int buscarIdProduto(Connection conn, String nomeProduto) throws SQLException {
        String sql = "SELECT id_produto FROM produto WHERE nome_produto = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeProduto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_produto");
            }
        }
        throw new SQLException("Produto/serviço não encontrado: " + nomeProduto);
    }

    private int buscarIdUnidade(Connection conn, String nomeUnidade) throws SQLException {
        String sql = "SELECT id_unidade FROM unidade WHERE nome_unidade = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeUnidade);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_unidade");
            }
        }
        throw new SQLException("Unidade não encontrada: " + nomeUnidade);
    }

    private int buscarOuCriarProdutoUnidade(Connection conn, int idUnidade, int idProduto) throws SQLException {
        String buscar = "SELECT id_produto_unidade FROM produto_unidade WHERE id_unidade = ? AND id_produto = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(buscar)) {
            stmt.setInt(1, idUnidade);
            stmt.setInt(2, idProduto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_produto_unidade");
            }
        }

        String inserir = "INSERT INTO produto_unidade (id_unidade, id_produto, produto_utilizado) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(inserir, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, idUnidade);
            stmt.setInt(2, idProduto);
            stmt.setInt(3, 0); // ajuste conforme o tipo/regra da coluna
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Não foi possível criar o vínculo produto/unidade.");
    }

    private int buscarOuCriarDisponibilidade(Connection conn, LocalDate data, LocalTime horario,
                                             int idProdutoUnidade) throws SQLException {
        // 1) tenta encontrar uma disponibilidade já existente
        String buscar = """
            SELECT id_disponibilidade
            FROM disponibilidade
            WHERE data_disponibilidade = ?
              AND hora_inicio = ?
              AND id_produto_unidade = ?
            ORDER BY id_disponibilidade
            LIMIT 1
            """;
        try (PreparedStatement stmt = conn.prepareStatement(buscar)) {
            stmt.setDate(1, Date.valueOf(data));
            stmt.setTime(2, Time.valueOf(horario));
            stmt.setInt(3, idProdutoUnidade);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_disponibilidade");
            }
        }

        // 2) se não existir, cria uma nova
        String inserir = """
            INSERT INTO disponibilidade
                (total_vagas, hora_fim, data_disponibilidade, hora_inicio, id_produto_unidade)
            VALUES (1, ?, ?, ?, ?)
            """;
        try (PreparedStatement stmt = conn.prepareStatement(inserir, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTime(1, Time.valueOf(horario.plusHours(1)));
            stmt.setDate(2, Date.valueOf(data));
            stmt.setTime(3, Time.valueOf(horario));
            stmt.setInt(4, idProdutoUnidade);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        throw new SQLException("Não foi possível criar a disponibilidade.");
    }
    //Não mexa aqui otario
    public int contarPraticasDoMes(String emailInstrutor) {
        String sql = """
            SELECT COUNT(*)
            FROM agendamento a
            INNER JOIN usuario ui ON ui.id_usuario = a.id_usuario
            INNER JOIN instrutor i ON i.id_usuario = ui.id_usuario
            WHERE ui.email = ?
              AND a.data_hora_agendamento >= ?
              AND a.data_hora_agendamento < ?
            """;
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        return executarContagem(sql, emailInstrutor,
                Timestamp.valueOf(inicio.atStartOfDay()),
                Timestamp.valueOf(inicio.plusMonths(1).atStartOfDay()));
    }

    public int calcularPercentualVagasOcupadas(String emailInstrutor) {
        String sql = """
            SELECT COALESCE(SUM(d.total_vagas), 0)
            FROM disponibilidade d
            WHERE d.id_disponibilidade IN (
                SELECT a.id_disponibilidade
                FROM agendamento a
                INNER JOIN usuario ui ON ui.id_usuario = a.id_usuario
                WHERE ui.email = ?
                  AND a.data_hora_agendamento >= ?
                  AND a.data_hora_agendamento < ?
            )
            """;
        String sqlOcupadas = """
            SELECT COUNT(*)
            FROM agendamento a
            INNER JOIN usuario ui ON ui.id_usuario = a.id_usuario
            WHERE ui.email = ?
              AND a.situacao NOT IN ('Cancelado', 'Não Compareceu')
              AND a.data_hora_agendamento >= ?
              AND a.data_hora_agendamento < ?
            """;
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        Timestamp de = Timestamp.valueOf(inicio.atStartOfDay());
        Timestamp ate = Timestamp.valueOf(inicio.plusMonths(1).atStartOfDay());

        int totalVagas = executarContagem(sql, emailInstrutor, de, ate);
        int ocupadas = executarContagem(sqlOcupadas, emailInstrutor, de, ate);
        return totalVagas == 0 ? 0 : (int) Math.round(ocupadas * 100.0 / totalVagas);
    }

    private int executarContagem(String sql, String email, Timestamp de, Timestamp ate) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setTimestamp(2, de);
            stmt.setTimestamp(3, ate);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao executar contagem", e);
        }
    }
    public int contarTurmasAtivas(String emailInstrutor) {
        String sql = """
            SELECT COUNT(DISTINCT t.id_turma)
            FROM turma t
            INNER JOIN instrutor_gerencia_turma igt ON igt.id_turma = t.id_turma
            INNER JOIN usuario u ON u.id_usuario = igt.id_usuario
            WHERE u.email = ?
              AND t.situacao = 'A'
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailInstrutor);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar turmas ativas", e);
        }
    }
}
