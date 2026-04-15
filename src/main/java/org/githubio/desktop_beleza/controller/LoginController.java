package org.githubio.desktop_beleza.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
    protected void irParaAtualizarSenha()throws IOException {
       MainApplication.setRoot("atualizarSenha");
    }
    @FXML
    protected void onLoginButtonClick() throws IOException{
        String nomeDigitado = Email.getText();
        String senhaDigitada = Senha.getText();

        LoginDAO usuarioDao = new LoginDAO();
        boolean logado = usuarioDao.autenticarUsuario(nomeDigitado, senhaDigitada);

        if (logado==true) {
            IO.println("Login realizado com sucesso!");
        }
        else {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Verifique as informações inseridas");
            alerta.setHeaderText(null);
            alerta.setContentText("E-mail ou senha incorreto.");
            alerta.showAndWait();
        }
    }

}



