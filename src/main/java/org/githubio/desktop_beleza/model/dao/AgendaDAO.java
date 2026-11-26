package org.githubio.desktop_beleza.model.dao;

import org.githubio.desktop_beleza.config.DatabaseConnection;
import org.githubio.desktop_beleza.model.Agendamento;
import org.githubio.desktop_beleza.model.Modelo;
import org.githubio.desktop_beleza.model.Produto;
import org.githubio.desktop_beleza.model.Unidade;

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

    // CACHE EM MEMÓRIA PARA EVITAR CONSULTAS REPETIDAS AO BANCO
    private static List<Produto> cacheProdutos = null;
    private static List<Modelo> cacheModelos = null;
    private static List<Unidade> cacheUnidades = null;

    public static void limparCacheCombos() {
        cacheProdutos = null;
        cacheModelos = null;
        cacheUnidades = null;
    }

    public List<Produto> listarProdutos() {
        if (cacheProdutos != null && !cacheProdutos.isEmpty()) {
            return cacheProdutos;
        }
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT id_produto, nome_produto, descricao FROM produto ORDER BY id_produto ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Produto produto = new Produto(
                        rs.getInt("id_produto"),
                        rs.getString("nome_produto"),
                        rs.getString("descricao")
                );
                lista.add(produto);
            }
            cacheProdutos = lista;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar procedimentos", e);
        }
        return lista;
    }

    public List<Produto> listarProdutosPorUnidade(String nomeUnidade) {
        List<Produto> lista = new ArrayList<>();
        String sql = """
                SELECT DISTINCT p.id_produto, p.nome_produto, p.descricao
                FROM produto_unidade pu
                INNER JOIN produto p ON p.id_produto = pu.id_produto
                INNER JOIN unidade u ON u.id_unidade = pu.id_unidade
                WHERE u.nome_unidade = ?
                  AND u.situacao = 'A'
                ORDER BY p.nome_produto
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeUnidade);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto(
                            rs.getInt("id_produto"),
                            rs.getString("nome_produto"),
                            rs.getString("descricao")
                    );
                    lista.add(produto);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar procedimentos da unidade", e);
        }
        return lista;
    }

    public List<Modelo> listarModelos() {
        if (cacheModelos != null && !cacheModelos.isEmpty()) {
            return cacheModelos;
        }
        List<Modelo> lista = new ArrayList<>();
        String sql = """
                SELECT u.id_usuario, u.nome_usuario, u.email, m.id_modelo, m.data_nascimento, m.telefone
                FROM modelo m
                INNER JOIN usuario u ON u.id_usuario = m.id_usuario
                ORDER BY u.id_usuario
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Modelo modelo = new Modelo(
                        rs.getInt("id_modelo"),
                        rs.getString("nome_usuario"),
                        rs.getString("telefone"),
                        rs.getString("email"),
                        rs.getDate("data_nascimento")
                );
                lista.add(modelo);
            }
            cacheModelos = lista;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar modelos", e);
        }
        return lista;
    }

    public List<Unidade> listarUnidades() {
        if (cacheUnidades != null && !cacheUnidades.isEmpty()) {
            return cacheUnidades;
        }
        List<Unidade> lista = new ArrayList<>();
        String sql = "SELECT id_unidade, nome_unidade, situacao, endereco FROM unidade WHERE situacao = 'A' ORDER BY id_unidade";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Unidade unidade = new Unidade(
                        rs.getInt("id_unidade"),
                        rs.getString("nome_unidade"),
                        rs.getString("endereco"),
                        rs.getString("situacao")
                );
                lista.add(unidade);
            }
            cacheUnidades = lista;
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
                int idProdutoUnidade = buscarProdutoUnidade(conn, idUnidade, idProduto);

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
    public List<Agendamento> listarAgendamentos(String emailInstrutor) {
        String sql = consultaBase() + " ORDER BY data_hora_agendamento";
        return executarLista(sql, null, null);
    }

    public List<Agendamento> listarAgendamentosPorSemana(LocalDate inicio, LocalDate fim, String emailInstrutor) {
        String sql = consultaBase() + " WHERE data_hora_agendamento >= ? AND data_hora_agendamento < ? ORDER BY data_hora_agendamento";
        return executarLista(sql, inicio, fim);
    }

    private String consultaBase() {
        return "SELECT id_agendamento, data_agendamento, horario_agendamento, situacao, nome_modelo, nome_servico FROM vw_agendamentos_detalhados";
    }

    private List<Agendamento> executarLista(String sql, LocalDate inicio, LocalDate fim) {
        List<Agendamento> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (inicio != null && fim != null) {
                stmt.setTimestamp(1, Timestamp.valueOf(inicio.atStartOfDay()));
                stmt.setTimestamp(2, Timestamp.valueOf(fim.plusDays(1).atStartOfDay()));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate data = rs.getDate("data_agendamento").toLocalDate();
                    LocalTime horario = rs.getTime("horario_agendamento").toLocalTime();
                    lista.add(new Agendamento(
                            rs.getInt("id_agendamento"),
                            new LocalDateTime(data, horario),
                            rs.getString("situacao"),
                            new Produto(rs.getString("nome_servico"), null),
                            new Modelo(rs.getString("nome_modelo"), null, null, null)
                    ));
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

    public void editarAgendamento(Agendamento agendamento) {
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
                    stmtContexto.setInt(1, agendamento.getId());
                    try (ResultSet rs = stmtContexto.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Agendamento não encontrado.");
                        }
                        idUnidade = rs.getInt("id_unidade");
                    }
                }

                int idModelo = buscarIdModelo(conn, agendamento.getModelo().getNome());
                int idProduto = buscarIdProduto(conn, agendamento.getProduto().getNome());
                int idProdutoUnidade = buscarProdutoUnidade(conn, idUnidade, idProduto);
                LocalDateTime data_hora_agendamento = agendamento.getData_hora_agendamento();
                int idDisponibilidade = buscarOuCriarDisponibilidade(conn, data_hora_agendamento.toLocalDate(), data_hora_agendamento.toLocalTime(), idProdutoUnidade);

                try (PreparedStatement stmt = conn.prepareStatement(atualizar)) {
                    stmt.setTimestamp(1, Timestamp.valueOf(data_hora_agendamento));
                    stmt.setInt(2, idDisponibilidade);
                    stmt.setInt(3, idModelo);
                    stmt.setInt(4, agendamento.getId());
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
        throw new SQLException("Produto/procedimento não encontrado: " + nomeProduto);
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

    private int buscarProdutoUnidade(Connection conn, int idUnidade, int idProduto) throws SQLException {
        String buscar = """
                SELECT id_produto_unidade
                FROM produto_unidade
                WHERE id_unidade = ?
                  AND id_produto = ?
                LIMIT 1
                """;
        try (PreparedStatement stmt = conn.prepareStatement(buscar)) {
            stmt.setInt(1, idUnidade);
            stmt.setInt(2, idProduto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_produto_unidade");
            }
        }
        throw new SQLException("Este procedimento não está vinculado à unidade selecionada. Faça o vínculo na tela Unidades.");
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
            SELECT COALESCE(SUM(d.total_vagas), 0) AS total_vagas,
                   COALESCE(SUM(LEAST(x.ocupadas, d.total_vagas)), 0) AS vagas_ocupadas
            FROM disponibilidade d
            INNER JOIN (
                SELECT a.id_disponibilidade, COUNT(*) AS ocupadas
                FROM agendamento a
                INNER JOIN usuario ui ON ui.id_usuario = a.id_usuario
                WHERE ui.email = ?
                  AND a.situacao NOT IN ('Cancelado', 'Não Compareceu')
                  AND a.data_hora_agendamento >= ?
                  AND a.data_hora_agendamento < ?
                GROUP BY a.id_disponibilidade
            ) x ON x.id_disponibilidade = d.id_disponibilidade
            """;
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailInstrutor);
            stmt.setTimestamp(2, Timestamp.valueOf(inicio.atStartOfDay()));
            stmt.setTimestamp(3, Timestamp.valueOf(inicio.plusMonths(1).atStartOfDay()));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total_vagas");
                    int ocupadas = rs.getInt("vagas_ocupadas");
                    return total == 0 ? 0 : (int) Math.round(ocupadas * 100.0 / total);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular vagas ocupadas", e);
        }
        return 0;
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
    public List<Agendamento> buscarAgendamentos(LocalDate data, String unidade, String servico) {
        return buscarAgendamentos(data, unidade, servico, false);
    }

    public List<Agendamento> buscarHistorico(LocalDate data, String unidade, String servico) {
        return buscarAgendamentos(data, unidade, servico, true);
    }

    private List<Agendamento> buscarAgendamentos(LocalDate data, String unidade, String servico,
                                                 boolean historico) {
        StringBuilder sql = new StringBuilder("""
        SELECT a.id_agendamento,
               DATE_FORMAT(a.data_hora_agendamento, '%Y-%m-%d') AS data_agendamento,
               DATE_FORMAT(a.data_hora_agendamento, '%H:%i')    AS horario_agendamento,
               a.situacao,
               um.nome_usuario AS nome_modelo,
               p.nome_produto  AS nome_servico
        FROM agendamento a
        INNER JOIN modelo m             ON m.id_modelo = a.id_modelo
        INNER JOIN usuario um           ON um.id_usuario = m.id_usuario
        INNER JOIN disponibilidade d    ON d.id_disponibilidade = a.id_disponibilidade
        INNER JOIN produto_unidade pu   ON pu.id_produto_unidade = d.id_produto_unidade
        INNER JOIN produto p            ON p.id_produto = pu.id_produto
        INNER JOIN unidade un           ON un.id_unidade = pu.id_unidade
        WHERE 1 = 1
        """);

        List<Object> params = new ArrayList<>();

        LocalDate hoje = LocalDate.now();
        if (historico) {
            sql.append(" AND a.data_hora_agendamento < ?");
            params.add(Timestamp.valueOf(hoje.atStartOfDay()));
        } else {
            sql.append(" AND a.data_hora_agendamento >= ?");
            params.add(Timestamp.valueOf(hoje.atStartOfDay()));
        }

        if (data != null) {
            sql.append(" AND a.data_hora_agendamento >= ? AND a.data_hora_agendamento < ?");
            params.add(Timestamp.valueOf(data.atStartOfDay()));
            params.add(Timestamp.valueOf(data.plusDays(1).atStartOfDay()));
        }
        if (unidade != null) {
            sql.append(" AND un.nome_unidade = ?");
            params.add(unidade);
        }
        if (servico != null) {
            sql.append(" AND p.nome_produto = ?");
            params.add(servico);
        }
        sql.append(historico
                ? " ORDER BY a.data_hora_agendamento DESC"
                : " ORDER BY a.data_hora_agendamento");

        List<Agendamento> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Agendamento(
                            rs.getInt("id_agendamento"),
                            rs.getString("data_agendamento"),
                            rs.getString("horario_agendamento"),
                            rs.getString("situacao"),
                            rs.getString("nome_modelo"),
                            rs.getString("nome_servico")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar agendamentos", e);
        }
        return lista;
    }
}
