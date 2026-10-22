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

import java.awt.event.ActionEvent;
import java.io.IOException;

public class RecuperarSenhaController {

    @FXML private TextField txtEmail;

    @FXML private TextField txtCodigo;

    @FXML private PasswordField campoNovoSenha;
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

    private final Image imagemOlhoAberto = new Image(getClass().getResource("images/eye-open.png").toExternalForm());
    private final Image imagemOlhoFechado = new Image(getClass().getResource("images/eye-closed.png").toExternalForm());

    private final AtualizarSenhaDAO atualizarSenhaDAO = new AtualizarSenhaDAO();
    private final PasswordResetTokenDAO passwordResetTokenDAO = new PasswordResetTokenDAO();
    private final EmailService emailService = new EmailService();

    @FXML
    public void initialize() {
        if (campoNovoSenha != null) {
            configurarMecanismoOlhoSenha();
            configurarValidacoesSenhaTempoReal();
        }
    }

    @FXML
    public void onEnviarCodigo(ActionEvent event) {
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

        }
    }

    private void configurarMecanismoOlhoSenha() {
        campoNovaSenhaVisivel.textProperty().bindBidirectional(campoNovoSenha.textProperty());
        btnToggleNovaSenha.setOnAction(e -> {
            novaSenhaVisivel = !novaSenhaVisivel;
            campoNovaSenhaVisivel.setVisible(novaSenhaVisivel);
            campoNovaSenhaVisivel.setManaged(novaSenhaVisivel);
            campoNovoSenha.setVisible(!novaSenhaVisivel);
            campoNovoSenha.setManaged(!novaSenhaVisivel);
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
        campoNovoSenha.textProperty().addListener((obs, oldVal, newVal) -> atualizarEstadoValidacoes(newVal));
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