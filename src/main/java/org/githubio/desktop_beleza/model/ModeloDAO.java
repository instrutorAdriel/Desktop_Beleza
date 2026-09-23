package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ModeloDAO {


    public void cadastrar(Modelo modelo) {
        String buscarUsuario = "SELECT id_usuario FROM usuario WHERE email = ? ORDER BY id_usuario LIMIT 1";
        String inserirUsuario = "INSERT INTO usuario (email, nome_usuario, senha) VALUES (?, ?, ?)";
        String atualizarUsuario = "UPDATE usuario SET nome_usuario = ? WHERE id_usuario = ?";
        String verificarModelo = "SELECT 1 FROM modelo WHERE id_usuario = ? LIMIT 1";
        String inserirModelo = "INSERT INTO modelo (telefone, id_usuario) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Integer idUsuario = null;
                try (PreparedStatement stmtBusca = conn.prepareStatement(buscarUsuario)) {
                    stmtBusca.setString(1, modelo.getEmail());
                    try (ResultSet rs = stmtBusca.executeQuery()) {
                        if (rs.next()) idUsuario = rs.getInt("id_usuario");
                    }
                }

                if (idUsuario == null) {
                    try (PreparedStatement stmtUsuario = conn.prepareStatement(inserirUsuario, Statement.RETURN_GENERATED_KEYS)) {
                        stmtUsuario.setString(1, modelo.getEmail());
                        stmtUsuario.setString(2, modelo.getNome());
                        stmtUsuario.setString(3, java.util.UUID.randomUUID().toString());
                        stmtUsuario.executeUpdate();
                        try (ResultSet rs = stmtUsuario.getGeneratedKeys()) {
                            if (!rs.next()) throw new SQLException("Não foi possível obter o id_usuario do modelo.");
                            idUsuario = rs.getInt(1);
                        }

                    }
                } else {
                    try (PreparedStatement stmtAtualiza = conn.prepareStatement(atualizarUsuario)) {
                        stmtAtualiza.setString(1, modelo.getNome());
                        stmtAtualiza.setInt(2, idUsuario);
                        stmtAtualiza.executeUpdate();
                    }
                    try (PreparedStatement stmtVerifica = conn.prepareStatement(verificarModelo)) {
                        stmtVerifica.setInt(1, idUsuario);
                        try (ResultSet rs = stmtVerifica.executeQuery()) {
                            if (rs.next()) throw new SQLException("Já existe um modelo cadastrado com este e-mail.");
                        }
                    }
                }

                try (PreparedStatement stmtModelo = conn.prepareStatement(inserirModelo)) {
                    stmtModelo.setString(1, modelo.getTelefone());
                    stmtModelo.setInt(2, idUsuario);
                    stmtModelo.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar modelo", e);
        }
    }

    public List<Modelo> lerTodos() {
        String sql = """
                SELECT m.id_modelo, m.telefone, u.nome_usuario, u.email
                FROM modelo m
                INNER JOIN usuario u ON u.id_usuario = m.id_usuario
                ORDER BY u.nome_usuario
                """;
        List<Modelo> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Modelo(
                        rs.getInt("id_modelo"),
                        rs.getString("nome_usuario"),
                        rs.getString("telefone"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar modelos", e);
        }
        return lista;
    }

    public void atualizar(Modelo modelo) {
        String buscarUsuario = "SELECT id_usuario FROM modelo WHERE id_modelo = ?";
        String atualizarUsuario = "UPDATE usuario SET nome_usuario = ?, email = ? WHERE id_usuario = ?";
        String atualizarModelo = "UPDATE modelo SET telefone = ? WHERE id_modelo = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idUsuario;
                try (PreparedStatement stmtBusca = conn.prepareStatement(buscarUsuario)) {
                    stmtBusca.setInt(1, modelo.getId());
                    try (ResultSet rs = stmtBusca.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Modelo não encontrado.");
                        }
                        idUsuario = rs.getInt("id_usuario");
                    }
                }

                try (PreparedStatement stmtUsuario = conn.prepareStatement(atualizarUsuario);
                     PreparedStatement stmtModelo = conn.prepareStatement(atualizarModelo)) {
                    stmtUsuario.setString(1, modelo.getNome());
                    stmtUsuario.setString(2, modelo.getEmail());
                    stmtUsuario.setInt(3, idUsuario);
                    stmtUsuario.executeUpdate();

                    stmtModelo.setString(1, modelo.getTelefone());
                    stmtModelo.setInt(2, modelo.getId());
                    stmtModelo.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar modelo", e);
        }
    }

    public void excluir(int idModelo) {
        String buscarUsuario = "SELECT id_usuario FROM modelo WHERE id_modelo = ?";
        String excluirModelo = "DELETE FROM modelo WHERE id_modelo = ?";
        String excluirUsuario = """
                DELETE FROM usuario
                WHERE id_usuario = ?
                  AND NOT EXISTS (SELECT 1 FROM instrutor WHERE instrutor.id_usuario = usuario.id_usuario)
                """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idUsuario;
                try (PreparedStatement stmtBusca = conn.prepareStatement(buscarUsuario)) {
                    stmtBusca.setInt(1, idModelo);
                    try (ResultSet rs = stmtBusca.executeQuery()) {
                        if (!rs.next()) {
                            return;
                        }
                        idUsuario = rs.getInt("id_usuario");
                    }
                }

                try (PreparedStatement stmtModelo = conn.prepareStatement(excluirModelo)) {
                    stmtModelo.setInt(1, idModelo);
                    stmtModelo.executeUpdate();
                }

                try (PreparedStatement stmtUsuario = conn.prepareStatement(excluirUsuario)) {
                    stmtUsuario.setInt(1, idUsuario);
                    stmtUsuario.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir modelo. Verifique se ele possui agendamentos ou depoimentos vinculados.", e);
        }
    }
}
