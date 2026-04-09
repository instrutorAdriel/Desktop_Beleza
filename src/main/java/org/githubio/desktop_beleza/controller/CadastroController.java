package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.LoginDAO;

import java.io.IOException;

public class CadastroController {
    @FXML
    private TextField campoUsuario;
    @FXML
    private TextField campoMatricula;
    @FXML
    private TextField campoSenha;
    @FXML
    private TextField campoConfirmarSenha;


    // Metodo chamado ao clicar no botão "Voltar" no cadastro.fxml
    @FXML
    protected void onVoltarClick() throws IOException {
        // Volta para a tela principal
        MainApplication.setRoot("login");
    }

    // Metodo chamado ao clicar em "Salvar" (exemplo)
    @FXML
    protected void onSalvarClick() throws IOException {
        // Faça o que precisar aqui antes de trocar a tela
        IO.println("Cadastro salvo!");

        // Após salvar, volta para a tela principal
        MainApplication.setRoot("login");
    }
    @FXML
    public void onCadastrarClick() {

        // 1. Lê os valores digitados nos campos da tela
        //    getText() retorna o conteúdo atual do campo como String
        String usuario    = campoUsuario.getText();
        String matricula = campoMatricula.getText();
        String senha   = campoSenha.getText();

        // 2. Validação básica: verifica se algum campo está vazio
        //    isBlank() retorna true se a String for vazia ou só tiver espaços
        if (usuario.isBlank() || senha.isBlank()) {

            // Exibe uma janela de aviso para o usuário
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Campos obrigatórios");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, preencha todos os campos antes de cadastrar.");
            alerta.showAndWait();

            // Interrompe o metodo — não vai ao banco com dados incompletos
            return;
        }

        // 1. Get the text from YOUR specific field IDs
        String confirma = campoConfirmarSenha.getText();

        // 2. The Logic
        if (senha.isEmpty() || confirma.isEmpty()) {
            mostrarErro("Campos Vazios", "Por favor, preencha todos os campos.");
            return;
        } else if (!senha.equals(confirma)) {
            mostrarErro("Erro de Senha", "As senhas não coincidem!");
            return;
        } else {
            IO.println("Sucesso! Iniciando cadastro...");
            // Sua lógica de banco de dados aqui
        }

        // 3. Cria uma instância do LoginDAO
        //    O DAO é quem sabe como falar com o banco de dados.
        //    O Controller apenas coleta os dados da tela e os repassa.
        LoginDAO dao = new LoginDAO();

        // 4. Chama o metodo de cadastro passando os três campos
        //    A partir daqui, a responsabilidade passa para o LoginDAO
        dao.cadastrarUsuario(usuario, senha);

        // 5. Informa ao usuário que o cadastro foi realizado
        Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
        sucesso.setTitle("Cadastro realizado");
        sucesso.setHeaderText(null);
        sucesso.setContentText("Usuário '" + matricula + "' cadastrado com sucesso!");
        sucesso.showAndWait();

        // 6. Limpa os campos após o cadastro (boa prática de UX)
        campoUsuario.clear();
        campoMatricula.clear();
        campoSenha.clear();
        campoConfirmarSenha.clear();
    }

    // Helper method to keep your code clean
    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null); // Keeps it simple
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

}

