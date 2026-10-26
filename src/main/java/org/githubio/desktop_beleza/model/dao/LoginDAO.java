package org.githubio.desktop_beleza.model.dao;

import org.githubio.desktop_beleza.config.DatabaseConnection;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class LoginDAO {

    // Mesmo padrão usado em CadastroController.
    private static final Pattern EMAIL_INSTITUCIONAL =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@df\\.senac\\.br$", Pattern.CASE_INSENSITIVE);

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * O Desktop é a área do instrutor. O acesso é permitido quando a senha confere e:
     *  - o usuário já possui registro na tabela instrutor; ou
     *  - o e-mail é institucional (@df.senac.br), caso em que o registro de instrutor
     *    é criado automaticamente (ex.: conta criada pelo Web/Mobile).
     * Qualquer outro e-mail é recusado.
     */
    public boolean autenticarUsuario(String email, String senha) {
        String sql = """
                SELECT u.id_usuario, u.email, u.senha, i.id_instrutor
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
                    return false;
                }

                String senhaHash = rs.getString("senha");
                if (senhaHash == null || !ENCODER.matches(senha, senhaHash)) {
                    return false;
                }

                rs.getInt("id_instrutor");
                boolean jaEhInstrutor = !rs.wasNull();
                if (jaEhInstrutor) {
                    return true;
                }

                String emailCadastrado = rs.getString("email");
                if (emailCadastrado == null || !EMAIL_INSTITUCIONAL.matcher(emailCadastrado.trim()).matches()) {
                    return false;
                }

                criarInstrutor(conn, rs.getInt("id_usuario"));
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar usuário", e);
        }
    }

    private void criarInstrutor(Connection conn, int idUsuario) throws SQLException {
        String sql = """
                INSERT INTO instrutor (id_usuario)
                SELECT ? FROM DUAL
                WHERE NOT EXISTS (SELECT 1 FROM instrutor WHERE id_usuario = ?)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idUsuario);
            stmt.executeUpdate();
        }
    }
}