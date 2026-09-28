package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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

    @FXML
    private PasswordField Senha;

    @FXML
    private TextField SenhaVisible;

    @FXML
    private ImageView imgOlho;

    private boolean senhaVisivel = false;

    @FXML
    public void initialize() {

        SenhaVisible.textProperty().bindBidirectional(Senha.textProperty());

        // INÍCIO:
        // Senha escondida: ••••••••
        // Olho ABERTO
        senhaVisivel = false;

        Senha.setVisible(true);
        Senha.setManaged(true);

        SenhaVisible.setVisible(false);
        SenhaVisible.setManaged(false);

        imgOlho.setImage(
                new Image(
                        getClass().getResourceAsStream(
                                "/org/githubio/desktop_beleza/Imagens/olho-aberto.png"
                        )
                )
        );
    }

    @FXML
    protected void irParaCadastro() throws IOException {
        MainApplication.setRoot("cadastro");
    }

    @FXML
    protected void irParaAtualizarSenha() throws IOException {
        MainApplication.setRoot("atualizarsenha");
    }

    @FXML
    protected void onLoginButtonClick() throws IOException {

        String emailDigitado = Email.getText();
        String senhaDigitada = Senha.getText();

        if (emailDigitado == null || emailDigitado.trim().isEmpty()) {

            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Campo e-mail está em branco");
            erro.setHeaderText(null);
            erro.setContentText(
                    "O campo e-mail está em branco. Por favor insira o seu e-mail."
            );
            erro.showAndWait();

            return;
        }

        if (senhaDigitada == null || senhaDigitada.isEmpty()) {

            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Campo senha está em branco");
            erro.setHeaderText(null);
            erro.setContentText(
                    "O campo senha está em branco. Por favor insira a sua senha."
            );
            erro.showAndWait();

            return;
        }

        LoginDAO usuarioDao = new LoginDAO();

        boolean logado = usuarioDao.autenticarUsuario(
                emailDigitado,
                senhaDigitada
        );

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
    protected void alternarVisibilidadeSenha() {

        senhaVisivel = !senhaVisivel;

        if (senhaVisivel) {

            // SENHA VISÍVEL
            // Exemplo: 12345
            Senha.setVisible(false);
            Senha.setManaged(false);

            SenhaVisible.setVisible(true);
            SenhaVisible.setManaged(true);

            SenhaVisible.requestFocus();
            SenhaVisible.positionCaret(
                    SenhaVisible.getText().length()
            );

            // OLHO FECHADO
            imgOlho.setImage(
                    new Image(
                            getClass().getResourceAsStream(
                                    "/org/githubio/desktop_beleza/Imagens/olho-fechado.png"
                            )
                    )
            );

        } else {

            // SENHA ESCONDIDA
            // Exemplo: ••••••••
            SenhaVisible.setVisible(false);
            SenhaVisible.setManaged(false);

            Senha.setVisible(true);
            Senha.setManaged(true);

            Senha.requestFocus();
            Senha.positionCaret(
                    Senha.getText().length()
            );

            // OLHO ABERTO
            imgOlho.setImage(
                    new Image(
                            getClass().getResourceAsStream(
                                    "/org/githubio/desktop_beleza/Imagens/olho-aberto.png"
                            )
                    )
            );
        }
    }
}