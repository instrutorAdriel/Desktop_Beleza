package org.githubio.desktop_beleza.model.dao;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Disponibiliza um serviço na agenda de uma unidade (tabela disponibilidade).
 */
public class DisponibilidadeDAO {

    /**
     * Cria a disponibilidade do serviço na unidade, na data e horário informados.
     * Se o serviço ainda não estiver vinculado à unidade, o vínculo é criado.
     * Se já existir uma disponibilidade para a mesma data/horário de início,
     * as vagas informadas são somadas às existentes.
     *
     * @return true se uma nova disponibilidade foi criada; false se foram somadas vagas a uma existente.
     */
    public boolean disponibilizar(int idProduto, int idUnidade, LocalDate data,
                                  LocalTime inicio, LocalTime fim, int vagas) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idProdutoUnidade = buscarOuCriarProdutoUnidade(conn, idUnidade, idProduto);
                boolean criada = inserirOuSomarVagas(conn, idProdutoUnidade, data, inicio, fim, vagas);
                conn.commit();
                AgendaDAO.limparCacheCombos();
                return criada;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao disponibilizar serviço na agenda", e);
        }
    }

    private int buscarOuCriarProdutoUnidade(Connection conn, int idUnidade, int idProduto) throws SQLException {
        String buscar = "SELECT id_produto_unidade FROM produto_unidade WHERE id_unidade = ? AND id_produto = ? LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(buscar)) {
            stmt.setInt(1, idUnidade);
            stmt.setInt(2, idProduto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        String inserir = "INSERT INTO produto_unidade (id_unidade, id_produto, produto_utilizado) VALUES (?, ?, 0)";
        try (PreparedStatement stmt = conn.prepareStatement(inserir, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, idUnidade);
            stmt.setInt(2, idProduto);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Não foi possível vincular o serviço à unidade.");
    }

    private boolean inserirOuSomarVagas(Connection conn, int idProdutoUnidade, LocalDate data,
                                        LocalTime inicio, LocalTime fim, int vagas) throws SQLException {
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
            stmt.setTime(2, Time.valueOf(inicio));
            stmt.setInt(3, idProdutoUnidade);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int idDisponibilidade = rs.getInt(1);
                    String somar = "UPDATE disponibilidade SET total_vagas = total_vagas + ?, hora_fim = ? WHERE id_disponibilidade = ?";
                    try (PreparedStatement upd = conn.prepareStatement(somar)) {
                        upd.setInt(1, vagas);
                        upd.setTime(2, Time.valueOf(fim));
                        upd.setInt(3, idDisponibilidade);
                        upd.executeUpdate();
                    }
                    return false;
                }
            }
        }

        String inserir = """
                INSERT INTO disponibilidade
                    (total_vagas, hora_fim, data_disponibilidade, hora_inicio, id_produto_unidade)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(inserir)) {
            stmt.setInt(1, vagas);
            stmt.setTime(2, Time.valueOf(fim));
            stmt.setDate(3, Date.valueOf(data));
            stmt.setTime(4, Time.valueOf(inicio));
            stmt.setInt(5, idProdutoUnidade);
            stmt.executeUpdate();
        }
        return true;
    }
}