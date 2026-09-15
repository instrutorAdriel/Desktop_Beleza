package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UnidadeDAO {

    public List<Unidade> listarTodas() {

        String sql = """
                SELECT id_unidade,
                       nome_unidade,
                       endereco,
                       situacao
                FROM unidade
                ORDER BY nome_unidade
                """;

        List<Unidade> unidades = new ArrayList<>();

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {

            while (rs.next()) {

                Unidade unidade = new Unidade(
                        rs.getInt("id_unidade"),
                        rs.getString("nome_unidade"),
                        rs.getString("endereco"),
                        rs.getString("situacao")
                );

                unidades.add(unidade);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return unidades;
    }


    public void inserir(Unidade unidade) {

        String sql = """
                INSERT INTO unidade
                (nome_unidade, situacao, endereco)
                VALUES (?, ?, ?)
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, unidade.getNome());
            stmt.setString(2, unidade.getSituacao());
            stmt.setString(3, unidade.getEndereco());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void atualizar(Unidade unidade) {

        String sql = """
                UPDATE unidade
                SET nome_unidade = ?,
                    endereco = ?,
                    situacao = ?
                WHERE id_unidade = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, unidade.getNome());
            stmt.setString(2, unidade.getEndereco());
            stmt.setString(3, unidade.getSituacao());
            stmt.setInt(4, unidade.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void alterarSituacao(int id, String situacao) {

        String sql = """
                UPDATE unidade
                SET situacao = ?
                WHERE id_unidade = ?
                """;

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {

            stmt.setString(1, situacao);
            stmt.setInt(2, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}