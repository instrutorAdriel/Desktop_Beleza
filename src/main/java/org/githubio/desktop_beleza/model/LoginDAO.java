package org.githubio.desktop_beleza.model;

import org.githubio.desktop_beleza.config.DatabaseConnection;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDAO {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /** TEMPORÁRIO (diagnóstico): motivo da última falha de login, exibido na janela de erro. */
    private static String ultimoMotivo = "";

    public static String getUltimoMotivo() {
        return ultimoMotivo;
    }

    /**
     * Aceita qualquer usuário da tabela usuario (inclusive os criados no Web/Mobile).
     * Se o usuário ainda não for instrutor, o registro é criado após validar a senha,
     * pois o restante do Desktop depende da linha em instrutor.
     *
     * As mensagens [LOGIN] vão para o console (System.err) e NÃO exibem senha nem hash.
     */
    public boolean autenticarUsuario(String email, String senha) {
        ultimoMotivo = "";
        String sql = """
                SELECT u.id_usuario, u.senha, i.id_instrutor
                FROM usuario u
                LEFT JOIN instrutor i ON i.id_usuario = u.id_usuario
                WHERE u.email = ?
                ORDER BY (i.id_instrutor IS NULL), u.id_usuario
                LIMIT 1
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    ultimoMotivo = "E-mail não encontrado no banco: " + conn.getMetaData().getURL();
                    System.err.println("[LOGIN] " + ultimoMotivo);
                    return false;
                }

                int idUsuario = rs.getInt("id_usuario");
                String senhaHash = rs.getString("senha");

                if (senhaHash == null || !senhaHash.matches("\\$2[aby]?\\$\\d\\d\\$.{53}")) {
                    ultimoMotivo = "Usuário " + idUsuario + " existe, mas a coluna senha não tem formato BCrypt (tamanho="
                            + (senhaHash == null ? "null" : senhaHash.length()) + ").";
                    System.err.println("[LOGIN] " + ultimoMotivo);
                    return false;
                }

                if (!ENCODER.matches(senha, senhaHash)) {
                    ultimoMotivo = "Usuário " + idUsuario + " existe e o hash é BCrypt válido ("
                            + senhaHash.substring(0, 7) + "...), mas a senha digitada não confere.";
                    System.err.println("[LOGIN] " + ultimoMotivo);
                    return false;
                }

                rs.getInt("id_instrutor");
                if (rs.wasNull()) {
                    System.err.println("[LOGIN] Usuario " + idUsuario + ": senha OK, criando registro em instrutor.");
                    garantirInstrutor(conn, idUsuario);
                }
                return true;
            }
        } catch (SQLException e) {
            ultimoMotivo = "Erro de banco: " + e.getMessage();
            System.err.println("[LOGIN] " + ultimoMotivo);
            throw new RuntimeException("Erro ao autenticar usuário: " + e.getMessage(), e);
        }
    }

    private void garantirInstrutor(Connection conn, int idUsuario) throws SQLException {
        String sql = "INSERT INTO instrutor (id_usuario) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.executeUpdate();
        }
    }
}