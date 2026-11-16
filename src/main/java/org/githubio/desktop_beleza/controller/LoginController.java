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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;
import java.util.regex.Pattern;

import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.dao.LoginDAO;

public class LoginController {

    // =====================================================================
    // 1) CONSTANTES
    // =====================================================================

    // Apenas e-mails institucionais acessam o Desktop (mesmo padrão do cadastro)
    private static final Pattern EMAIL_INSTITUCIONAL =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@df\\.senac\\.br$", Pattern.CASE_INSENSITIVE);

    // =====================================================================
    // 2) COMPONENTES DO FXML (os nomes precisam bater com os fx:id do login.fxml)
    // =====================================================================

    @FXML private TextField Email;
    @FXML private PasswordField Senha;
    @FXML private TextField SenhaVisivel;
    @FXML private Button btnToggleNovaSenha; // CORRIGIDO: antes era btnToggleSenha (não batia com o FXML)
    @FXML private ImageView imgOlho;

    // =====================================================================
    // 3) ESTADO DA TELA
    // =====================================================================

    private boolean senhaEstaVisivel = false;

    // Trava para impedir login duplo enquanto uma autenticação está em andamento
    private boolean autenticando = false;

    // =====================================================================
    // 4) INICIALIZAÇÃO
    // =====================================================================

    @FXML
    private void initialize() {
        // Os dois campos de senha ficam sempre com o mesmo texto
        SenhaVisivel.textProperty().bindBidirectional(Senha.textProperty());
    }

    // =====================================================================
    // 5) NAVEGAÇÃO ENTRE TELAS
    // =====================================================================

    @FXML
    protected void irParaCadastro() throws IOException {
        MainApplication.setRoot("cadastro");
    }

    @FXML
    protected void irParaAtualizarSenha() throws IOException {
        MainApplication.setRoot("recuperar-senha-email");
    }

    // =====================================================================
    // 6) OLHINHO DA SENHA
    // =====================================================================

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

    // =====================================================================
    // 7) LOGIN (o método do botão "Login")
    // =====================================================================

    @FXML
    protected void onLoginButtonClick() {
        String emailDigitado = Email.getText().trim();
        String senhaDigitada = Senha.getText();

        // ---- 7.1) Validações rápidas (rodam na thread da UI, não precisam de Task) ----
        if (emailDigitado.isEmpty()) {
            mostrarErro("Campo e-mail está em branco",
                    "O campo e-mail está em branco. Por favor insira o seu e-mail.");
            return;
        }
        if (senhaDigitada.isEmpty()) {
            mostrarErro("Campo senha está em branco",
                    "O campo senha está em branco. Por favor insira a sua senha.");
            return;
        }
        if (!EMAIL_INSTITUCIONAL.matcher(emailDigitado).matches()) {
            mostrarErro("E-mail fora do padrão",
                    "O e-mail precisa ser do domínio @df.senac.br.");
            return;
        }

        // ---- 7.2) Trava contra clique duplo ----
        if (autenticando) {
            return;
        }
        autenticando = true;

        // ---- 7.3) Cria o popup "Autenticando..." (ainda não aparece) ----
        Stage popupLoading = criarPopupLoading();

        // ---- 7.4) Task: consulta ao banco em BACKGROUND ----
        // REGRA: dentro do call() NUNCA mexa em tela (Label, Alert, Stage, setRoot...).
        Task<Boolean> tarefaLogin = new Task<>() {
            @Override
            protected Boolean call() {
                return new LoginDAO().autenticarUsuario(emailDigitado, senhaDigitada);
            }
        };

        // ---- 7.5) Quando a consulta termina (já volta na thread da UI) ----
        tarefaLogin.setOnSucceeded(evento -> {
            autenticando = false;

            if (tarefaLogin.getValue()) {
                // Login OK: guarda o usuário e carrega a agenda EM BACKGROUND.
                // O popup continua aberto e só fecha quando a agenda estiver pronta.
                MainApplication.setUsuario(emailDigitado);
                MainApplication.setRootAsync(
                        "Telaagenda",
                        popupLoading::close,              // sucesso: fecha o popup
                        erroCarga -> {                    // falha ao carregar a agenda
                            popupLoading.close();
                            erroCarga.printStackTrace();
                            new Alert(Alert.AlertType.ERROR, "Não foi possível abrir a agenda.").showAndWait();
                        });
            } else {
                // Credenciais erradas: fecha o popup e avisa
                popupLoading.close();
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle("Informações incorretas");
                alerta.setHeaderText(null);
                alerta.setContentText("E-mail ou senha incorreto.");
                alerta.showAndWait();
            }
        });

        // ---- 7.6) Se a consulta lançou exceção (banco fora do ar, rede, etc.) ----
        tarefaLogin.setOnFailed(evento -> {
            popupLoading.close();
            autenticando = false;
            tarefaLogin.getException().printStackTrace(); // veja o motivo real no console

            mostrarErro("Erro ao autenticar", "Não foi possível realizar o login. Tente novamente.");
        });

        // ---- 7.7) Dispara: mostra o popup ANTES e depois inicia a thread ----
        popupLoading.show(); // show(), nunca showAndWait()
        Thread threadLogin = new Thread(tarefaLogin);
        threadLogin.setDaemon(true); // não impede o app de fechar
        threadLogin.start();
    }

    // =====================================================================
    // 8) MÉTODOS AUXILIARES (sempre DENTRO da classe, fora dos outros métodos)
    // =====================================================================

    // Evita repetir o mesmo bloco de Alert de erro várias vezes
    private void mostrarErro(String titulo, String mensagem) {
        Alert erro = new Alert(Alert.AlertType.ERROR);
        erro.setTitle(titulo);
        erro.setHeaderText(null);
        erro.setContentText(mensagem);
        erro.showAndWait();
    }

    // Popup modal "Autenticando...": bloqueia a tela de login até a resposta chegar
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
            // Centraliza o popup em cima da janela de login
            popup.setX(janelaLogin.getX() + (janelaLogin.getWidth() - popup.getWidth()) / 2);
            popup.setY(janelaLogin.getY() + (janelaLogin.getHeight() - popup.getHeight()) / 2);
        });
        return popup;
    }
}