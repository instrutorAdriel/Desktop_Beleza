package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.AtualizarSenhaDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class AtualizarSenhaController {
    @FXML
    protected void voltarParaLogin() throws IOException {
        MainApplication.setRoot("login");
    }

    @FXML
    private TextField campoEmail;
    @FXML
    private TextField campoNovaSenha;
    @FXML
    private TextField campoConfirmarSenha;

    @FXML
    public void onAlterarSenha() {
        String email = campoEmail.getText();
        String NovaSenha = campoNovaSenha.getText();
        String ConfirmarSenha = campoConfirmarSenha.getText();

        // 1. Verifica se algum campo está vazio
        if (email.isBlank() || NovaSenha.isBlank() || ConfirmarSenha.isBlank()) {
            mostrarErro("Campos obrigatórios", "Por favor, preencha todos os campos.");
        }
        // 2. Verifica o domínio do e-mail (O "!" significa "NÃO contém")
        else if (!email.contains("@df.senac.br")) {
            mostrarErro("E-mail Inválido", "Use um e-mail institucional (@df.senac.br).");
        }
        // 3. Verifica se as senhas são DIFERENTES (O "!" antes de NovaSenha)
        else if (!NovaSenha.equals(ConfirmarSenha)) {
            mostrarErro("Erro de Senha", "As senhas não coincidem!");
        }
        // 4. Se passar por tudo, sucesso!
        else {
            IO.println("Sucesso! Iniciando cadastro...");
            // Sua lógica de banco de dados aqui

            AtualizarSenhaDAO dao = new AtualizarSenhaDAO();

            boolean existe = dao.instrutorExiste(campoEmail.getText());
            IO.println(existe);
            if (existe){
                String senhaHash = BCrypt.hashpw(campoNovaSenha.getText(), BCrypt.gensalt());
                if (dao.atualizarSenha(campoEmail.getText(),senhaHash)){
                    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
                    alerta.setTitle("Senha Atualizada");
                    alerta.setHeaderText(null);
                    alerta.setContentText("Sua senha foi atualizada com sucesso! Retorne para tela de login e entre na sua conta com a nova senha.");
                    alerta.showAndWait();
                    limparCamposFormulario();
                }
            }
        }

    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void limparCamposFormulario(){
        campoEmail.clear();
        campoNovaSenha.clear();
        campoConfirmarSenha.clear();
    }
}



