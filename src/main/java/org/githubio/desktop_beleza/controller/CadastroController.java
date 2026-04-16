package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.CadastroDAO;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;

public class CadastroController {
    @FXML
    private TextField campoEmail;
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
    public void onCadastrarClick() throws IOException {

        // 1. Lê os valores digitados nos campos da tela
        //    getText() retorna o conteúdo atual do campo como String
        String email    = campoEmail.getText();
        String senha   = campoSenha.getText();
        String regex = "^[a-zA-Z0-9._%+-]+@df\\.senac\\.br$";

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

        // 2. Validação básica: verifica se algum campo está vazio
        //    isBlank() retorna true se a String for vazia ou só tiver espaços
        //  matcher.matches()
        if (email.isBlank() || senha.isBlank()) {

            // Exibe uma janela de aviso para o usuário
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Campos obrigatórios");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, preencha todos os campos antes de cadastrar.");
            alerta.showAndWait();

            // Interrompe o metodo — não vai ao banco com dados incompletos
            return;
        }

        if (!matcher.matches()){
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("E-mail fora do padrão");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, preencha o e-mail seguindo o padrão \"@df.senac.br\".");
            alerta.showAndWait();
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

        CadastroDAO dao = new CadastroDAO();

        dao.cadastrarUsuario(email, senha);

        // 5. Informa ao usuário que o cadastro foi realizado
        Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
        sucesso.setTitle("Cadastro realizado");
        sucesso.setHeaderText(null);
        sucesso.setContentText("Usuário '"  + "' cadastrado com sucesso!");
        sucesso.showAndWait();

        // 6. Limpa os campos após o cadastro (boa prática de UX)
        campoEmail.clear();
        campoSenha.clear();
        campoConfirmarSenha.clear();

       MainApplication.setRoot("login");
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

}

