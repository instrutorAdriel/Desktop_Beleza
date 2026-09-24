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
import org.githubio.desktop_beleza.model.AtualizarSenhaDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class AtualizarSenhaController {

    @FXML private TextField campoEmail;

    @FXML private PasswordField campoNovaSenha;
    @FXML private TextField campoNovaSenhaVisivel;
    @FXML private Button btnToggleNovaSenha;
    @FXML private ImageView imgOlhoNova;

    @FXML private PasswordField campoConfirmarSenha;
    @FXML private TextField campoConfirmarSenhaVisivel;
    @FXML private Button btnToggleConfirmarSenha;
    @FXML private ImageView imgOlhoConfirmar;

    // Componentes de validação em tempo real
    @FXML private VBox vboxValidacoes;
    @FXML private Label lblMaiuscula;
    @FXML private Label lblMinuscula;
    @FXML private Label lblNumero;
    @FXML private Label lblEspecial;

    // Carregamento das imagens personalizadas da pasta Imagens
    // Carrega as imagens usando o URL absoluto do classpath
    // Recua duas pastas a partir de 'controller' para chegar à pasta 'Imagens' na raiz do pacote
    // Carrega as imagens de forma absoluta a partir da raiz do classpath (resources)
    private final Image imagemOlhoAberto = new Image(getClass().getResource("/org/githubio/desktop_beleza/Imagens/eyeOpen 1.png").toExternalForm());
    private final Image imagemOlhoFechado = new Image(getClass().getResource("/org/githubio/desktop_beleza/Imagens/eyeClosed 1.png").toExternalForm());

    private boolean novaSenhaVisivel = false;
    private boolean confirmarSenhaVisivel = false;

    @FXML
    public void initialize() {
        // Sincronização dos campos de texto (oculto e visível)
        campoNovaSenhaVisivel.textProperty().bindBidirectional(campoNovaSenha.textProperty());

        btnToggleNovaSenha.setOnAction(event -> {
            novaSenhaVisivel = !novaSenhaVisivel;
            campoNovaSenhaVisivel.setVisible(novaSenhaVisivel);
            campoNovaSenhaVisivel.setManaged(novaSenhaVisivel);
            campoNovaSenha.setVisible(!novaSenhaVisivel);
            campoNovaSenha.setManaged(!novaSenhaVisivel);

            // Troca a imagem do botão consoante o estado
            imgOlhoNova.setImage(novaSenhaVisivel ? imagemOlhoFechado : imagemOlhoAberto);
        });

        campoConfirmarSenhaVisivel.textProperty().bindBidirectional(campoConfirmarSenha.textProperty());

        btnToggleConfirmarSenha.setOnAction(event -> {
            confirmarSenhaVisivel = !confirmarSenhaVisivel;
            campoConfirmarSenhaVisivel.setVisible(confirmarSenhaVisivel);
            campoConfirmarSenhaVisivel.setManaged(confirmarSenhaVisivel);
            campoConfirmarSenha.setVisible(!confirmarSenhaVisivel);
            campoConfirmarSenha.setManaged(!confirmarSenhaVisivel);

            imgOlhoConfirmar.setImage(confirmarSenhaVisivel ? imagemOlhoFechado : imagemOlhoAberto);
        });

        // Ouvintes para validar a senha e mostrar a VBox dinamicamente
        campoNovaSenha.textProperty().addListener((observable, oldValue, newValue) -> {
            atualizarEstadoValidacoes(newValue);
        });

        campoNovaSenhaVisivel.textProperty().addListener((observable, oldValue, newValue) -> {
            atualizarEstadoValidacoes(newValue);
        });
    }

    private void atualizarEstadoValidacoes(String senha) {
        if (senha == null || senha.isEmpty()) {
            vboxValidacoes.setVisible(false);
            vboxValidacoes.setManaged(false);
            return;
        }

        // Mostra o bloco responsivo assim que o utilizador começa a escrever
        vboxValidacoes.setVisible(true);
        vboxValidacoes.setManaged(true);

        // Critério 1: Letra Maiúscula
        if (senha.matches(".*[A-Z].*")) {
            lblMaiuscula.setText("✔ Letra maiúscula");
            lblMaiuscula.setTextFill(javafx.scene.paint.Color.web("#5cb85c"));
        } else {
            lblMaiuscula.setText("❌ Letra maiúscula");
            lblMaiuscula.setTextFill(javafx.scene.paint.Color.web("#d9534f"));
        }

        // Critério 2: Letra Minúscula
        if (senha.matches(".*[a-z].*")) {
            lblMinuscula.setText("✔ Letra minúscula");
            lblMinuscula.setTextFill(javafx.scene.paint.Color.web("#5cb85c"));
        } else {
            lblMinuscula.setText("❌ Letra minúscula");
            lblMinuscula.setTextFill(javafx.scene.paint.Color.web("#d9534f"));
        }

        // Critério 3: Número
        if (senha.matches(".*[0-9].*")) {
            lblNumero.setText("✔ Número");
            lblNumero.setTextFill(javafx.scene.paint.Color.web("#5cb85c"));
        } else {
            lblNumero.setText("❌ Número");
            lblNumero.setTextFill(javafx.scene.paint.Color.web("#d9534f"));
        }

        // Critério 4: Caractere Especial
        if (senha.matches(".*[^a-zA-Z0-9].*")) {
            lblEspecial.setText("✔ Caractere especial");
            lblEspecial.setTextFill(javafx.scene.paint.Color.web("#5cb85c"));
        } else {
            lblEspecial.setText("❌ Caractere especial");
            lblEspecial.setTextFill(javafx.scene.paint.Color.web("#d9534f"));
        }
    }

    @FXML
    protected void voltarParaLogin() throws IOException {
        MainApplication.setRoot("login");
    }

    @FXML
    public void onAlterarSenha() {
        String email = campoEmail.getText();
        String novaSenha = campoNovaSenha.getText();
        String confirmarSenha = campoConfirmarSenha.getText();

        if (email.isBlank() || novaSenha.isBlank() || confirmarSenha.isBlank()) {
            mostrarErro("Campos obrigatórios", "Por favor, preencha todos os campos.");
            return;
        }

        if (!email.contains("@df.senac.br")) {
            mostrarErro("E-mail Inválido", "Use um e-mail institucional (@df.senac.br).");
            return;
        }

        // Validação estrita dos requisitos antes de gravar
        boolean senhaForte = novaSenha.matches(".*[A-Z].*") &&
                novaSenha.matches(".*[a-z].*") &&
                novaSenha.matches(".*[0-9].*") &&
                novaSenha.matches(".*[^a-zA-Z0-9].*");

        if (!senhaForte) {
            mostrarErro("Senha Fraca", "A nova senha deve conter letra maiúscula, minúscula, número e caractere especial.");
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            mostrarErro("Erro de Senha", "As senhas não coincidem!");
            return;
        }

        AtualizarSenhaDAO dao = new AtualizarSenhaDAO();
        boolean existe = dao.instrutorExiste(email);

        if (existe) {
            try {
                String senhaHash = BCrypt.hashpw(novaSenha, BCrypt.gensalt());

                if (dao.atualizarSenha(email, senhaHash)) {
                    Alert alerta = new Alert(Alert.AlertType.INFORMATION);
                    alerta.setTitle("Sucesso");
                    alerta.setHeaderText(null);
                    alerta.setContentText("Senha atualizada com sucesso para: " + email);
                    alerta.showAndWait();

                    MainApplication.setRoot("login");
                }
            } catch (IOException e) {
                mostrarErro("Erro de Navegação", "Não foi possível retornar à tela de login.");
            }
        } else {
            mostrarErro("E-mail Não Cadastrado", "O e-mail informado não consta em nosso sistema.");
            campoEmail.requestFocus();
        }
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}