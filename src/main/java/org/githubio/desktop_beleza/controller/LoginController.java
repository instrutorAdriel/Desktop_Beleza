package org.githubio.desktop_beleza.controller;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.LoginDAO;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField Usuário;
    @FXML
    private PasswordField Senha;
    @FXML
    public void botaoDeTroca()throws IOException {
        MainApplication.setRoot("cadastro");
    }
    @FXML
    public void onLoginButtonClick() throws IOException{
        String nomeDigitado = Usuário.getText();
        String senhaDigitada = Senha.getText();

        LoginDAO usuarioDao = new LoginDAO();
        boolean logado = usuarioDao.autenticarUsuario(nomeDigitado, senhaDigitada);

        if (logado==true) {
            IO.println("Login realizado com sucesso!");
        }
        else {
            IO.println("Senha inválida ou usuário.");
        }
    }
}



