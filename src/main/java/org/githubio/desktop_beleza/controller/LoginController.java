package org.githubio.desktop_beleza.controller;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.CadastroDAO;

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
        // Correção: corrigir o nome da variavel para emailDigitado
        // Correção: Muda as variaveis de acordo com os campos da tela. Troque de usuário para email
        String nomeDigitado = Usuário.getText();
        String senhaDigitada = Senha.getText();

        CadastroDAO usuarioDao = new CadastroDAO();
        boolean logado = usuarioDao.autenticarUsuario(nomeDigitado, senhaDigitada);

        if (logado==true) {
            IO.println("Login realizado com sucesso!");
        }
        else {
            IO.println("Senha inválida ou usuário.");
        }
    }
}



