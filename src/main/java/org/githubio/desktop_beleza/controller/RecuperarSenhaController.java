package org.githubio.desktop_beleza.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.dao.AtualizarSenhaDAO;
import org.githubio.desktop_beleza.model.dao.PasswordResetTokenDAO;
import org.githubio.desktop_beleza.service.EmailService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.regex.Pattern;

public class RecuperarSenhaController {

    private static final Pattern EMAIL_VALIDO =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final SecureRandom GERADOR_CODIGO = new SecureRandom();

    @FXML private TextField txtEmail;
    @FXML private Button btnEnviarCodigo;

    @FXML private TextField txtCodigo;
    @FXML private Button btnConfirmarCodigo;

    @FXML private PasswordField campoNovaSenha;
    @FXML private TextField campoNovaSenhaVisivel;
    @FXML private Button btnToggleNovaSenha;
    @FXML private ImageView imgOlhoNova;

    @FXML private PasswordField campoConfirmarSenha;
    @FXML private TextField campoConfirmarSenhaVisivel;
    @FXML private Button btnToggleConfirmarSenha;
    @FXML private ImageView imgOlhoConfirmar;

    @FXML private Button btnAlterarSenha;

    @FXML private VBox vboxValidacoes;
    @FXML private Label lblMaiuscula;
    @FXML private Label lblMinuscula;
    @FXML private Label lblNumero;
    @FXML private Label lblEspecial;

    private static volatile int idUsuarioEmRecuperacao;

    private boolean novaSenhaVisivel = false;
    private boolean confirmarSenhaVisivel = false;

    private final Image imagemOlhoAberto = new Image(MainApplication.class.getResource("images/eye-open.png").toExternalForm());
    private final Image imagemOlhoFechado = new Image(MainApplication.class.getResource("images/eye-closed.png").toExternalForm());

    private final AtualizarSenhaDAO atualizarSenhaDAO = new AtualizarSenhaDAO();
    private final PasswordResetTokenDAO passwordResetTokenDAO = new PasswordResetTokenDAO();
    @FXML
    public void initialize() {
        if (campoNovaSenha != null) {
            configurarMecanismoOlhoSenha();
            configurarValidacoesSenhaTempoReal();
        }
    }

    @FXML
    public void onEnviarCodigo() {
        String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";

        if (email.isBlank()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo Obrigatório", "Por favor, informe o e-mail.");
            return;
        }

        if (!EMAIL_VALIDO.matcher(email).matches()) {
            mostrarAlerta(Alert.AlertType.ERROR, "E-mail inválido", "Informe um endereço de e-mail válido.");
            return;
        }

        txtEmail.setDisable(true);
        btnEnviarCodigo.setDisable(true);
        btnEnviarCodigo.setText("Enviando...");


        idUsuarioEmRecuperacao = 0;
        Task<Void> tarefaEnvio = new Task<>() {
            @Override
            protected Void call() {
                Integer idUsuario = atualizarSenhaDAO.buscarIdUsuarioPorEmail(email);
                idUsuarioEmRecuperacao = idUsuario != null ? idUsuario : 0;

                if (idUsuario != null) {
                    String codigoOtp = String.format("%06d", GERADOR_CODIGO.nextInt(1_000_000));
                    passwordResetTokenDAO.salvarCodigoOtp(idUsuario, codigoOtp);
                    new EmailService().enviarCodigoRecuperacao(email, codigoOtp);
                }

                return null;
            }
        };

        tarefaEnvio.setOnSucceeded(event -> {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Solicitação Enviada",
                    "Se o e-mail informado estiver cadastrado, o código de verificação será enviado.");
            try {
                MainApplication.setRoot("recuperar-senha-codigo");
            } catch (IOException e) {
                restaurarEstadoBotao();
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir a tela de verificação.");
            }
        });

        tarefaEnvio.setOnFailed(event -> {
            Throwable e = tarefaEnvio.getException();
            if (e != null) e.printStackTrace();

            idUsuarioEmRecuperacao = 0;
            restaurarEstadoBotao();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro no Envio", "Não foi possível enviar o código de recuperação.");
        });

        Thread thread = new Thread(tarefaEnvio);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void onConfirmarCodigo() throws IOException {
        String codigoDigitado = txtCodigo.getText() != null ? txtCodigo.getText().trim() : "";

        if (!codigoDigitado.matches("\\d{6}")) {
            mostrarAlerta(Alert.AlertType.ERROR, "Código inválido", "Informe o código de 6 dígitos recebido por e-mail.");
            return;
        }

        boolean valido;
        try {
            valido = idUsuarioEmRecuperacao > 0
                    && passwordResetTokenDAO.validarEConsumirCodigoOtp(idUsuarioEmRecuperacao, codigoDigitado);
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível validar o código. Tente novamente.");
            return;
        }

        if (!valido) {
            mostrarAlerta(Alert.AlertType.ERROR, "Código Inválido", "O código digitado é incorreto ou já expirou.");
            return;
        }

        MainApplication.setRoot("recuperar-senha");
    }

    @FXML
    public void onAlterarSenha() {
        if (idUsuarioEmRecuperacao <= 0) {
            mostrarAlerta(Alert.AlertType.ERROR, "Sessão inválida", "Solicite um novo código de recuperação.");
            voltarParaLogin();
            return;
        }

        String novaSenha = campoNovaSenha.getText();
        String confirmarSenha = campoConfirmarSenha.getText();

        if (novaSenha == null || novaSenha.isBlank() || confirmarSenha == null || confirmarSenha.isBlank()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campos Obrigatórios", "Preencha a nova senha e a confirmação.");
            return;
        }

        boolean senhaForte = novaSenha.matches(".*[A-Z].*") &&
                novaSenha.matches(".*[a-z].*") &&
                novaSenha.matches(".*[0-9].*") &&
                novaSenha.matches(".*[^a-zA-Z0-9].*");

        if (!senhaForte) {
            mostrarAlerta(Alert.AlertType.ERROR, "Senha Fraca", "A senha deve conter maiúscula, minúscula, número e caractere especial.");
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Divergência de Senha", "As senhas não coincidem.");
            return;
        }

        String senhaHash = new BCryptPasswordEncoder().encode(novaSenha);

        if (atualizarSenhaDAO.atualizarSenha(idUsuarioEmRecuperacao, senhaHash)) {
            passwordResetTokenDAO.inativarTokensDoUsuario(idUsuarioEmRecuperacao);
            idUsuarioEmRecuperacao = 0;

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Sua senha foi alterada com sucesso.");
            voltarParaLogin();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao Atualizar", "Não foi possível atualizar a senha.");
        }
    }

    @FXML
    public void voltarParaLogin() {
        idUsuarioEmRecuperacao = 0;
        try {
            MainApplication.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configurarMecanismoOlhoSenha() {
        campoNovaSenhaVisivel.textProperty().bindBidirectional(campoNovaSenha.textProperty());
        btnToggleNovaSenha.setOnAction(e -> {
            novaSenhaVisivel = !novaSenhaVisivel;
            campoNovaSenhaVisivel.setVisible(novaSenhaVisivel);
            campoNovaSenhaVisivel.setManaged(novaSenhaVisivel);
            campoNovaSenha.setVisible(!novaSenhaVisivel);
            campoNovaSenha.setManaged(!novaSenhaVisivel);
            imgOlhoNova.setImage(novaSenhaVisivel ? imagemOlhoFechado : imagemOlhoAberto);
        });

        campoConfirmarSenhaVisivel.textProperty().bindBidirectional(campoConfirmarSenha.textProperty());
        btnToggleConfirmarSenha.setOnAction(e -> {
            confirmarSenhaVisivel = !confirmarSenhaVisivel;
            campoConfirmarSenhaVisivel.setVisible(confirmarSenhaVisivel);
            campoConfirmarSenhaVisivel.setManaged(confirmarSenhaVisivel);
            campoConfirmarSenha.setVisible(!confirmarSenhaVisivel);
            campoConfirmarSenha.setManaged(!confirmarSenhaVisivel);
            imgOlhoConfirmar.setImage(confirmarSenhaVisivel ? imagemOlhoFechado : imagemOlhoAberto);
        });
    }

    private void configurarValidacoesSenhaTempoReal() {
        campoNovaSenha.textProperty().addListener((obs, oldVal, newVal) -> atualizarEstadoValidacoes(newVal));
        campoNovaSenhaVisivel.textProperty().addListener((obs, oldVal, newVal) -> atualizarEstadoValidacoes(newVal));
    }

    private void atualizarEstadoValidacoes(String senha) {
        if (senha == null || senha.isEmpty()) {
            vboxValidacoes.setVisible(false);
            vboxValidacoes.setManaged(false);
            return;
        }

        vboxValidacoes.setVisible(true);
        vboxValidacoes.setManaged(true);

        lblMaiuscula.setText(senha.matches(".*[A-Z].*") ? "✔ Letra maiúscula" : "❌ Letra maiúscula");
        lblMaiuscula.setTextFill(senha.matches(".*[A-Z].*") ? javafx.scene.paint.Color.web("#5cb85c") : javafx.scene.paint.Color.web("#d9534f"));

        lblMinuscula.setText(senha.matches(".*[a-z].*") ? "✔ Letra minúscula" : "❌ Letra minúscula");
        lblMinuscula.setTextFill(senha.matches(".*[a-z].*") ? javafx.scene.paint.Color.web("#5cb85c") : javafx.scene.paint.Color.web("#d9534f"));

        lblNumero.setText(senha.matches(".*[0-9].*") ? "✔ Número" : "❌ Número");
        lblNumero.setTextFill(senha.matches(".*[0-9].*") ? javafx.scene.paint.Color.web("#5cb85c") : javafx.scene.paint.Color.web("#d9534f"));

        lblEspecial.setText(senha.matches(".*[^a-zA-Z0-9].*") ? "✔ Caractere especial" : "❌ Caractere especial");
        lblEspecial.setTextFill(senha.matches(".*[^a-zA-Z0-9].*") ? javafx.scene.paint.Color.web("#5cb85c") : javafx.scene.paint.Color.web("#d9534f"));
    }

    private void restaurarEstadoBotao() {
        txtEmail.setDisable(false);
        btnEnviarCodigo.setDisable(false);
        btnEnviarCodigo.setText("Enviar Código");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}