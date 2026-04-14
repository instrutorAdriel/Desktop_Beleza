package org.githubio.desktop_beleza.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.githubio.desktop_beleza.config.DatabaseConnection;

public class ModeloDAO {
    public List<String> lerUsuario(String user) {
        String sql = "SELECT nome_cliente, telefone, email_cliente FROM usuarios WHERE nome_usuario = ?";
        List<String> usuarios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String info = rs.getString("nome_cliente") + " - " + rs.getString("email_cliente");
                    usuarios.add(info);

                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário", e);
        }
        return usuarios;
    }public void cadastrarModelo(String nome, String telefone, String email) {

        // Comando SQL de inserção.
        // Os três ? serão substituídos pelos valores reais logo abaixo.
        // Usar ? (em vez de concatenar strings) protege contra SQL Injection.
        String sql = "INSERT INTO tb_clientes (nome_cliente, email_cliente, telefone) VALUES (?, ?, ?)";
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
            stmt.setString(1, nome);

            // 4. Preenche o 2º ? com o login escolhido pelo usuário
            //stmt.setString(2, usuario);

            // 5. Preenche o 3º ? com a senha escolhida pelo usuário
            //    Em um projeto real, a senha deveria ser criptografada antes
            //    de chegar aqui (ex.: usando BCrypt).
            stmt.setString(2, email);
            stmt.setString(3, telefone);
            // 6. Executa o INSERT no banco de dados
            //    executeUpdate() é usado para INSERT, UPDATE e DELETE
            //    (diferente de executeQuery(), que é usado para SELECT)
            stmt.executeUpdate();

            // Se chegou até aqui sem exceção, o cadastro foi realizado com sucesso
            IO.println("Modelo cadastrado com sucesso!");

        } catch (SQLException e) {
            // Se algo der errado (ex.: usuário duplicado, banco offline),
            // o erro é exibido no console para facilitar a depuração.
            e.printStackTrace();
        }
    }
}
