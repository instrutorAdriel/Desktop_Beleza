package org.githubio.desktop_beleza.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.IOException;
import java.util.regex.Pattern;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.dao.LoginDAO;

public class LoginController {
    // Mesmo padrão usado no cadastro: apenas e-mails institucionais acessam o Desktop.
    private static final Pattern EMAIL_INSTITUCIONAL =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@df\\.senac\\.br$", Pattern.CASE_INSENSITIVE);

    @FXML
    private TextField Email;

    @FXML
    private PasswordField Senha;

    // Trava para impedir que o login seja disparado mais de uma vez enquanto autentica
    private boolean autenticando = false;

    @FXML
    private TextField SenhaVisivel;

    @FXML
    private Button btnToggleSenha;

    @FXML
    private ImageView imgOlho;

    private boolean senhaEstaVisivel = false;

    @FXML
    private void initialize() {
        SenhaVisivel.textProperty().bindBidirectional(Senha.textProperty());
    }

    @FXML
    protected void irParaCadastro()throws IOException {
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
                ? "/org/githubio/desktop_beleza/images/eye-closed.png"
                : "/org/githubio/desktop_beleza/images/eye-open.png";

        imgOlho.setImage(new Image(getClass().getResourceAsStream(imagemOlho)));
    }

    @FXML
    protected void onLoginButtonClick() {
        String emailDigitado = Email.getText().trim();
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

        if (!EMAIL_INSTITUCIONAL.matcher(emailDigitado.trim()).matches()) {
            erro.setTitle("E-mail fora do padrão");
            erro.setHeaderText(null);
            erro.setContentText("O e-mail precisa ser do domínio @df.senac.br.");
            erro.showAndWait();
            return;
        }

        if (autenticando) {
            return; // já existe uma tentativa de login em andamento
        }
        autenticando = true;

        Stage popupLoading = criarPopupLoading();

        // A consulta ao banco roda fora da thread da interface, para o popup poder animar
        Task<Boolean> tarefaLogin = new Task<>() {
            @Override
            protected Boolean call() {
                return new LoginDAO().autenticarUsuario(emailDigitado, senhaDigitada);
            }
        };

        tarefaLogin.setOnSucceeded(evento -> {
            popupLoading.close();
            autenticando = false;

            if (tarefaLogin.getValue()) {
                //IO.println("Login realizado com sucesso!");
                MainApplication.setUsuario(emailDigitado);
                try {
                    MainApplication.setRoot("Telaagenda");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle("As informações inseridas não está corretas");
                alerta.setHeaderText(null);
                alerta.setContentText("E-mail ou senha incorreto.");
                alerta.showAndWait();
            }
        });

        tarefaLogin.setOnFailed(evento -> {
            popupLoading.close();
            autenticando = false;
            tarefaLogin.getException().printStackTrace();

            Alert falha = new Alert(Alert.AlertType.ERROR);
            falha.setTitle("Erro ao autenticar");
            falha.setHeaderText(null);
            falha.setContentText("Não foi possível realizar o login. Tente novamente.");
            falha.showAndWait();
        });

        popupLoading.show();
        Thread threadLogin = new Thread(tarefaLogin);
        threadLogin.setDaemon(true);
        threadLogin.start();
    }

    // Popup modal de "Autenticando...": bloqueia a tela de login até a resposta chegar
    private Stage criarPopupLoading() {
        ProgressIndicator indicador = new ProgressIndicator();
        indicador.setPrefSize(48, 48);

        Label texto = new Label("Autenticando...");

        VBox conteudo = new VBox(12, indicador, texto);
        conteudo.setAlignment(Pos.CENTER);
        conteudo.setPrefSize(200, 130);
        conteudo.setStyle("-fx-background-color: white; -fx-border-color: #004587; -fx-border-width: 1;");

        Window janelaLogin = Email.getScene().getWindow();

        Stage popup = new Stage(StageStyle.UNDECORATED);
        popup.initOwner(janelaLogin);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setScene(new Scene(conteudo));
        popup.setOnCloseRequest(evento -> evento.consume()); // não deixa fechar manualmente
        popup.setOnShown(evento -> {
            popup.setX(janelaLogin.getX() + (janelaLogin.getWidth() - popup.getWidth()) / 2);
            popup.setY(janelaLogin.getY() + (janelaLogin.getHeight() - popup.getHeight()) / 2);
        });
        return popup;
    }

    @FXML
    protected void irParaAtualizarSenha() throws IOException {
        MainApplication.setRoot("recuperar-senha-email");
    }
}