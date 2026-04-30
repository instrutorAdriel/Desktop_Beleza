package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.LoginDAO;

public class LoginController {
    @FXML
    private TextField Email;
    @FXML
    private PasswordField Senha;
    @FXML
    protected void irParaCadastro()throws IOException {
        MainApplication.setRoot("cadastro");
    }
    @FXML
    protected void onLoginButtonClick() throws IOException{
        String emailDigitado = Email.getText();
        String senhaDigitada = Senha.getText();
        
        // Alerta do tipo ERRO
        Alert erro = new Alert(Alert.AlertType.ERROR);
        if (emailDigitado.isEmpty()){
            erro.setTitle("Campo e-mail está em branco");
            erro.setHeaderText(null);
            erro.setContentText("O campo e-mail está em branco. Por favor insira o seu e-mail.");
            erro.showAndWait();
            return; // Ele sai da função quando o usuário fechar a caixa de alerta
        } else if (senhaDigitada.isEmpty()){
            erro.setTitle("Campo senha está em branco");
            erro.setHeaderText(null);
            erro.setContentText("O campo senha está em branco. Por favor insira a sua senha.");
            erro.showAndWait();
            return; // Ele sai da função quando o usuário fechar a caixa de alerta
        }

        LoginDAO usuarioDao = new LoginDAO();
        boolean logado = usuarioDao.autenticarUsuario(emailDigitado, senhaDigitada);

        if (logado==true) {
            //IO.println("Login realizado com sucesso!");
            MainApplication.setUsuario(emailDigitado);
            MainApplication.setRoot("Telaagenda");
        }
        else {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("As informações inseridas não está corretas");
            alerta.setHeaderText(null);
            alerta.setContentText("E-mail ou senha incorreto.");
            alerta.showAndWait();
        }
    }

    @FXML
    protected void irParaAtualizarSenha() throws IOException {
        MainApplication.setRoot("AtualizarSenha");
    }
}



