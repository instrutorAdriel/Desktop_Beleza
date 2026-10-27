package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.ColorAdjust;
import java.io.IOException;
import java.util.regex.Pattern;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.LoginDAO;

public class LoginController {
    // Mesmo padrão usado no cadastro: apenas e-mails institucionais acessam o Desktop.
    private static final Pattern EMAIL_INSTITUCIONAL =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@df\\.senac\\.br$", Pattern.CASE_INSENSITIVE);

    @FXML
    private TextField Email;

    @FXML
    private PasswordField Senha;

    @FXML
    private TextField SenhaVisivel;

    @FXML
    private Button btnToggleSenha;

    @FXML
    private ImageView imgOlho;

    @FXML
    private ImageView logoDecorativa;

    private boolean senhaEstaVisivel = false;

    @FXML
    private void initialize() {
        SenhaVisivel.textProperty().bindBidirectional(Senha.textProperty());


    }

    @FXML
    protected void irParaCadastro() throws IOException {
        MainApplication.setRoot("cadastro");
    }

    @FXML
    protected void alternarVisibilidadeSenha() {
        senhaEstaVisivel = !senhaEstaVisivel;

        Senha.setVisible(!senhaEstaVisivel);
        Senha.setManaged(!senhaEstaVisivel);

        SenhaVisivel.setVisible(senhaEstaVisivel);
        SenhaVisivel.setManaged(senhaEstaVisivel);

        String imagemOlho = senhaEstaVisivel
                ? "/org/githubio/desktop_beleza/Imagens/eyeClosed 1.png"
                : "/org/githubio/desktop_beleza/Imagens/eyeOpen 1.png";

        imgOlho.setImage(new Image(getClass().getResourceAsStream(imagemOlho)));
    }

    @FXML
    protected void onLoginButtonClick() throws IOException {
        String emailDigitado = Email.getText().trim();
        String senhaDigitada = Senha.getText();

        Alert erro = new Alert(Alert.AlertType.ERROR);

        if (emailDigitado.isEmpty()) {
            erro.setTitle("Campo e-mail está em branco");
            erro.setHeaderText(null);
            erro.setContentText("O campo e-mail está em branco. Por favor insira o seu e-mail.");
            erro.showAndWait();
            return;
        } else if (senhaDigitada.isEmpty()) {
            erro.setTitle("Campo senha está em branco");
            erro.setHeaderText(null);
            erro.setContentText("O campo senha está em branco. Por favor insira a sua senha.");
            erro.showAndWait();
            return;
        }

        if (!EMAIL_INSTITUCIONAL.matcher(emailDigitado.trim()).matches()) {
            erro.setTitle("E-mail fora do padrão");
            erro.setHeaderText(null);
            erro.setContentText("O e-mail precisa ser do domínio @df.senac.br.");
            erro.showAndWait();
            return;
        }

        if (!emailDigitado.trim().toLowerCase().endsWith("@df.senac.br")) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("E-mail fora do padrão");
            alerta.setHeaderText(null);
            alerta.setContentText("Para entrar nesse sistema é necessário utilizar um e-mail '@df.senac.br'");
            alerta.showAndWait();
            return;
        }

        LoginDAO usuarioDao = new LoginDAO();
        boolean logado = usuarioDao.autenticarUsuario(emailDigitado, senhaDigitada);

        if (logado) {
            MainApplication.setUsuario(emailDigitado);
            MainApplication.setRoot("Telaagenda");
        } else {
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