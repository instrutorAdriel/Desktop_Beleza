package org.githubio.desktop_beleza.controller;

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

public class RecuperarSenhaController {

    @FXML private TextField txtEmail;

    @FXML private TextField txtCodigo;

    @FXML private PasswordField campoNovaSenha;
    @FXML private TextField campoNovaSenhaVisivel;
    @FXML private Button btnToggleNovaSenha;
    @FXML private ImageView imgOlhoNova;

    @FXML private PasswordField campoConfirmarSenha;
    @FXML private TextField campoConfirmarSenhaVisivel;
    @FXML private Button btnToggleConfirmarSenha;
    @FXML private ImageView imgOlhoConfirmar;

    @FXML private VBox vboxValidacoes;
    @FXML private Label lblMaiuscula;
    @FXML private Label lblMinuscula;
    @FXML private Label lblNumero;
    @FXML private Label lblEspecial;

    private String emailEmRecuperacao;
    private int idUsuarioEmRecuperacao;

    private boolean novaSenhaVisivel = false;
    private boolean confirmarSenhaVisivel = false;

    private final Image imagemOlhoAberto = new Image(MainApplication.class.getResource("images/eye-open.png").toExternalForm());
    private final Image imagemOlhoFechado = new Image(MainApplication.class.getResource("images/eye-closed.png").toExternalForm());

    private final AtualizarSenhaDAO atualizarSenhaDAO = new AtualizarSenhaDAO();
    private final PasswordResetTokenDAO passwordResetTokenDAO = new PasswordResetTokenDAO();
    private final EmailService emailService = new EmailService();

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

        if (!email.contains("@df.senac.br")) {
            mostrarAlerta(Alert.AlertType.ERROR, "E-mail inválido", "Utilize o e-mail institucional.");
            return;
        }

        if (!atualizarSenhaDAO.instrutorExiste(email)) {
            mostrarAlerta(Alert.AlertType.INFORMATION,"Solicitação Enviada",
                    "Se o e-mail informado estiver cadastrado, o código de verificação será enviado.");
            return;
        }

        try {
            this.emailEmRecuperacao = email;
            this.idUsuarioEmRecuperacao = atualizarSenhaDAO.buscarIdUsuarioPorEmail(email);

            String codigoOtp = String.format("%06d", new java.util.Random().nextInt(999999));

            passwordResetTokenDAO.salvarCodigoOtp(this.idUsuarioEmRecuperacao, codigoOtp);

            new Thread(() -> {
                try {
                    emailService.enviarCodigoRecuperacao(emailEmRecuperacao, codigoOtp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            mostrarAlerta(Alert.AlertType.INFORMATION, "Código Enviado", "Enviamos um código de recuperação para o seu e-mail.");
            MainApplication.setRoot("recuperar-senha-codigo");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro no Envio", "Não foi possível gerar o código de recuperação.");
        }
    }

    @FXML
    public void onConfirmarCodigo() throws IOException {
        String codigoDigitado = txtCodigo.getText() != null ? txtCodigo.getText().trim() : "";

        if (codigoDigitado.isBlank()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Campo Obrigatório", "Por favor, informe o código recebido.");
            return;
        }

        boolean valido = passwordResetTokenDAO.validarCodigoOtp(this.idUsuarioEmRecuperacao, codigoDigitado);

        if (!valido) {
            mostrarAlerta(Alert.AlertType.ERROR, "Código Inválido", "O código digitado é incorreto ou já expirou.");
            return;
        }

        MainApplication.setRoot("recuperar-senha");
    }

    @FXML
    public void onAlterarSenha() throws IOException{
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

        if (atualizarSenhaDAO.atualizarSenha(emailEmRecuperacao, senhaHash)) {
            passwordResetTokenDAO.inativarTokensDoUsuario(idUsuarioEmRecuperacao);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Sua senha foi alterada com sucesso.");
            voltarParaLogin();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao Atualizar", "Não foi possível atualizar a senha.");
        }
    }

    @FXML
    public void voltarParaLogin() {
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

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}