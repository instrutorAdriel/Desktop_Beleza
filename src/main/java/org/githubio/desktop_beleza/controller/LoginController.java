package org.githubio.desktop_beleza.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.LoginDAO;

public class LoginController {
    @FXML
    private TextField Email;

    @FXML
    private PasswordField Senha;

    @FXML
    private TextField SenhaVisible; // Campo de texto para mostrar a senha em texto plano

    private boolean senhaVisivel = false;

    @FXML
    public void initialize() {
        // Sincroniza o texto entre o PasswordField e o TextField visível automaticamente
        if (SenhaVisible != null && Senha != null) {
            SenhaVisible.textProperty().bindBidirectional(Senha.textProperty());
        }
    }

    @FXML
    protected void irParaCadastro() throws IOException {
        MainApplication.setRoot("cadastro");
    }

    @FXML
    protected void onLoginButtonClick() throws IOException {
        String emailDigitado = Email.getText();
        // Pega a senha do campo correto dependendo se está visível ou oculta
        String senhaDigitada = senhaVisivel ? SenhaVisible.getText() : Senha.getText();

        // Alerta do tipo ERRO
        Alert erro = new Alert(Alert.AlertType.ERROR);
        if (emailDigitado.isEmpty()){
            erro.setTitle("Campo e-mail está em branco");
            erro.setHeaderText(null);
            erro.setContentText("O campo e-mail está em branco. Por favor insira o seu e-mail.");
            erro.showAndWait();
            return;
        } else if (senhaDigitada.isEmpty()){
            erro.setTitle("Campo senha está em branco");
            erro.setHeaderText(null);
            erro.setContentText("O campo senha está em branco. Por favor insira a sua senha.");
            erro.showAndWait();
            return;
        }

        LoginDAO usuarioDao = new LoginDAO();
        boolean logado = usuarioDao.autenticarUsuario(emailDigitado, senhaDigitada);

        if (logado) {
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
    private ImageView imgOlho; // Referência à imagem do olho

    @FXML
    protected void alternarVisibilidadeSenha() {
        senhaVisivel = !senhaVisivel;

        if (senhaVisivel) {
            // Mostra o texto plano e oculta a senha pontilhada
            SenhaVisible.setVisible(true);
            SenhaVisible.setManaged(true);
            Senha.setVisible(false);
            Senha.setManaged(false);
            SenhaVisible.toFront();

            // Altera para o ícone de olho aberto
            if (imgOlho != null) {
                imgOlho.setImage(new Image(getClass().getResourceAsStream("/org/githubio/desktop_beleza/Imagens/olho-aberto.png")));
            }
        } else {
            // Mostra a senha pontilhada e oculta o texto plano
            Senha.setVisible(true);
            Senha.setManaged(true);
            SenhaVisible.setVisible(false);
            SenhaVisible.setManaged(false);
            Senha.toFront();

            // Altera para o ícone de olho fechado
            if (imgOlho != null) {
                imgOlho.setImage(new Image(getClass().getResourceAsStream("/org/githubio/desktop_beleza/Imagens/olho-fechado.png")));
            }
        }
    }
    @FXML
    protected void irParaAtualizarSenha() throws IOException {
        MainApplication.setRoot("atualizarsenha");
    }

    public void alternarVisibilidadeSenhaPorBotao(ActionEvent actionEvent) {
    }
}
