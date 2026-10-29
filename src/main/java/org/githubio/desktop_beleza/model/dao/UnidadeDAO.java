package org.githubio.desktop_beleza.model.dao;

import org.githubio.desktop_beleza.config.DatabaseConnection;
import org.githubio.desktop_beleza.model.Unidade;
import org.githubio.desktop_beleza.model.Servico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UnidadeDAO {

    public List<Unidade> listarTodas() {
        String sql = "SELECT id_unidade, nome_unidade, endereco, situacao FROM unidade ORDER BY nome_unidade";
        List<Unidade> unidades = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                unidades.add(new Unidade(
                        rs.getInt("id_unidade"),
                        rs.getString("nome_unidade"),
                        rs.getString("endereco"),
                        rs.getString("situacao")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar com o banco na operação de Unidade", e);
        }

        return unidades;
    }


    public void inserir(Unidade unidade) {
        String sql = """
            INSERT INTO unidade
            (nome_unidade, situacao, endereco)
            VALUES (?, ?, ?)
            """;

        // Adicionado Statement.RETURN_GENERATED_KEYS no prepareStatement
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, unidade.getNome());
            stmt.setString(2, unidade.getSituacao());
            stmt.setString(3, unidade.getEndereco());
            stmt.executeUpdate();

            // Recupera o id_unidade auto-incrementado pelo MySQL
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    unidade.setId(rs.getInt(1)); // Atribui o ID gerado de volta ao DTO
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir unidade no banco de dados", e);
        }
    }


    public void atualizar(Unidade unidade) {
        String sql = "UPDATE unidade SET nome_unidade = ?, endereco = ?, situacao = ? WHERE id_unidade = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, unidade.getNome());
            stmt.setString(2, unidade.getEndereco());
            stmt.setString(3, unidade.getSituacao());
            stmt.setInt(4, unidade.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar com o banco na operação de Unidade", e);
        }
    }


    public void alterarSituacao(int id, String situacao) {
        String sql = "UPDATE unidade SET situacao = ? WHERE id_unidade = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, situacao);
            stmt.setInt(2, id);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar com o banco na operação de Unidade", e);
        }
    }

    public List<Servico> listarServicosVinculados(int idUnidade) {
        String sql = """
                SELECT DISTINCT p.id_produto, p.nome_produto, p.descricao
                FROM produto_unidade pu
                INNER JOIN produto p ON p.id_produto = pu.id_produto
                WHERE pu.id_unidade = ?
                ORDER BY p.nome_produto
                """;
        List<Servico> servicos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUnidade);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    servicos.add(new Servico(
                            rs.getInt("id_produto"),
                            rs.getString("nome_produto"),
                            rs.getString("descricao")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar serviços da unidade", e);
        }

        return servicos;
    }


    /**
     * Cria o vínculo entre um serviço (produto) e a unidade que o oferece,
     * na tabela produto_unidade.
     *
     * @return true se o vínculo foi criado; false se já existia.
     */
    public boolean vincularServico(int idUnidade, int idProduto) {

        String buscar = """
                SELECT 1
                FROM produto_unidade
                WHERE id_unidade = ?
                  AND id_produto = ?
                LIMIT 1
                """;

        String inserir = """
                INSERT INTO produto_unidade (id_unidade, id_produto, produto_utilizado)
                VALUES (?, ?, 0)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement stmt = conn.prepareStatement(buscar)) {

                stmt.setInt(1, idUnidade);
                stmt.setInt(2, idProduto);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(inserir)) {

                stmt.setInt(1, idUnidade);
                stmt.setInt(2, idProduto);

                stmt.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao vincular serviço à unidade", e);
        }
    }

    public void desvincularServico(int idUnidade, int idProduto) {
        String sql = """
                DELETE pu
                FROM produto_unidade pu
                WHERE pu.id_unidade = ?
                  AND pu.id_produto = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM disponibilidade d
                      WHERE d.id_produto_unidade = pu.id_produto_unidade
                  )
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUnidade);
            stmt.setInt(2, idProduto);
            if (stmt.executeUpdate() == 0) {
                throw new IllegalStateException(
                        "Este serviço possui disponibilidades associadas e não pode ser desvinculado."
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Não foi possível desvincular o serviço. Ele pode estar associado a disponibilidades ou agendamentos.",
                    e
            );
        }
    }
}