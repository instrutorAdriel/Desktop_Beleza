package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.IOException;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.LoginDAO;

public class LoginController {
    @FXML
    private TextField Email;

    // Elementos da Senha
    @FXML
    private PasswordField Senha;
    @FXML
    private TextField SenhaVisivel; // Campo que mostra a senha em texto puro
    @FXML
    private Button btnToggleSenha;
    @FXML
    private ImageView imgOlho;

    // Imagens do olho
    private final Image imagemOlhoAberto = new Image(getClass().getResource("/org/githubio/desktop_beleza/Imagens/eyeOpen 1.png").toExternalForm());
    private final Image imagemOlhoFechado = new Image(getClass().getResource("/org/githubio/desktop_beleza/Imagens/eyeClosed 1.png").toExternalForm());

    private boolean senhaVisivel = false;

    @FXML
    public void initialize() {
        // Sincroniza o que é digitado entre o PasswordField e o TextField
        if (SenhaVisivel != null && Senha != null) {
            SenhaVisivel.textProperty().bindBidirectional(Senha.textProperty());
        }

        // Configura a ação do botão do olho
        if (btnToggleSenha != null) {
            btnToggleSenha.setOnAction(event -> {
                senhaVisivel = !senhaVisivel;

                // Alterna a visibilidade
                SenhaVisivel.setVisible(senhaVisivel);
                SenhaVisivel.setManaged(senhaVisivel);
                Senha.setVisible(!senhaVisivel);
                Senha.setManaged(!senhaVisivel);

                // Troca a imagem
                imgOlho.setImage(senhaVisivel ? imagemOlhoFechado : imagemOlhoAberto);
            });
        }
    }

    @FXML
    protected void irParaCadastro() throws IOException {
        MainApplication.setRoot("cadastro");
    }

    @FXML
    protected void onLoginButtonClick() throws IOException {
        String emailDigitado = Email.getText();
        String senhaDigitada = Senha.getText(); // Graças ao bindBidirectional, sempre terá o valor correto

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
            alerta.setTitle("As informações inseridas não estão corretas");
            alerta.setHeaderText(null);
            alerta.setContentText("E-mail ou senha incorreto.");
            alerta.showAndWait();
        }
    }

    @FXML
    protected void irParaAtualizarSenha() throws IOException {
        MainApplication.setRoot("atualizarsenha");
    }
}