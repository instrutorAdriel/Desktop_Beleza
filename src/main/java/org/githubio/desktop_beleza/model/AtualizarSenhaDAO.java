package org.githubio.desktop_beleza.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.githubio.desktop_beleza.model.AtualizarSenhaDAO;
import org.githubio.desktop_beleza.config.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

public class AtualizarSenhaDAO {
    public List<String> lerUsuario(String user) {
        String sql = "SELECT email_instrutor, senha FROM tb_instrutor WHERE nome_cliente = ?";
        List<String> usuarios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String info = rs.getString("nome_instrutor") + " - " + rs.getString("senha");
                    usuarios.add(info);

                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário", e);
        }
        return usuarios;
    }

    public void cadastrarUsuario(String nome_instrutor, String senha) {

        // Comando SQL de inserção.
        // Os três ? serão substituídos pelos valores reais logo abaixo.
        // Usar ? (em vez de concatenar strings) protege contra SQL Injection.
        String sql = "INSERT INTO tb_instrutor (email_instrutor, senha) VALUES (?, ?)";
        String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
        try (
                // 1. Abre a conexão com o banco de dados
                //    DatabaseConfig.getConnection() retorna um objeto Connection
                //    pronto para uso, usando as configurações já definidas na classe.
                Connection conn = DatabaseConnection.getConnection();

                // 2. Prepara o comando SQL
                //    PreparedStatement "compila" o SQL no banco antes de enviar os dados,
                //    tornando a execução mais segura e eficiente.
                PreparedStatement stmt = conn.prepareStatement(sql)

        ) {
            // 3. Preenche o 1º ponto de interrogação com o valor do parâmetro "nome"
            //    setString(posição, valor) → posição começa em 1, não em 0
            stmt.setString(1, nome_instrutor);

            // 4. Preenche o 2º ? com o login escolhido pelo usuário
            //stmt.setString(2, usuario);

            // 5. Preenche o 3º ? com a senha escolhida pelo usuário
            //    Em um projeto real, a senha deveria ser criptografada antes
            //    de chegar aqui (ex.: usando BCrypt).
            stmt.setString(2, senhaHash);

            // 6. Executa o INSERT no banco de dados
            //    executeUpdate() é usado para INSERT, UPDATE e DELETE
            //    (diferente de executeQuery(), que é usado para SELECT)
            stmt.executeUpdate();

            // Se chegou até aqui sem exceção, o cadastro foi realizado com sucesso
            IO.println("Usuário cadastrado com sucesso!");

        } catch (SQLException e) {
            // Se algo der errado (ex.: usuário duplicado, banco offline),
            // o erro é exibido no console para facilitar a depuração.
            e.printStackTrace();
        }
    }

    public boolean autenticarUsuario(String nome, String senha) {
        String sql = "SELECT senha FROM tb_instrutor WHERE email_instrutor = ?";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, nome);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String senhaHash = rs.getString("senha");

                    // checkpw compara a senha digitada com o hash salvo no banco
                    return BCrypt.checkpw(senha, senhaHash);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao autenticar usuário", e);
        }

        return false; // usuário não encontrado
    }

    public boolean instrutorExiste(String email) {
        // 1. A Query busca se existe um instrutor com esse e-mail
        String sql = "SELECT * FROM tb_instrutor WHERE email_instrutor = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt  = conn.prepareStatement(sql);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            // Retorna true se encontrar o registro, false se não
            return rs.next();

        } catch (SQLException e) {
            System.out.println("Erro ao validar e-mail: " + e.getMessage());
            return false;
        }

    }

    public boolean atualizarSenha(String email, String senhaHash) {
        String sql = "UPDATE tb_instrutor SET senha = ? WHERE email_instrutor = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, senhaHash);
            stmt.setString(2, email);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar: " + e.getMessage());
            return false;
        }
    }
}

