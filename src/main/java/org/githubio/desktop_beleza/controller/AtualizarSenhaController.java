package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.AtualizarSenhaDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class AtualizarSenhaController {

    @FXML
    private TextField campoEmail;

    // Nova Senha (oculta e visível) e botão
    @FXML
    private PasswordField campoNovaSenha;
    @FXML
    private TextField campoNovaSenhaVisivel;
    @FXML
    private Button btnToggleNovaSenha;

    // Confirmar Senha (oculta e visível) e botão
    @FXML
    private PasswordField campoConfirmarSenha;
    @FXML
    private TextField campoConfirmarSenhaVisivel;
    @FXML
    private Button btnToggleConfirmarSenha;

    private boolean novaSenhaVisivel = false;
    private boolean confirmarSenhaVisivel = false;

    @FXML
    public void initialize() {
        // Sincroniza o texto digitado na Nova Senha bidirecionalmente
        campoNovaSenhaVisivel.textProperty().bindBidirectional(campoNovaSenha.textProperty());

        btnToggleNovaSenha.setOnAction(event -> {
            novaSenhaVisivel = !novaSenhaVisivel;
            campoNovaSenhaVisivel.setVisible(novaSenhaVisivel);
            campoNovaSenhaVisivel.setManaged(novaSenhaVisivel);
            campoNovaSenha.setVisible(!novaSenhaVisivel);
            campoNovaSenha.setManaged(!novaSenhaVisivel);

            btnToggleNovaSenha.setText(novaSenhaVisivel ? "🙈" : "👁");
        });

        // Sincroniza o texto digitado no Confirmar Senha bidirecionalmente
        campoConfirmarSenhaVisivel.textProperty().bindBidirectional(campoConfirmarSenha.textProperty());

        btnToggleConfirmarSenha.setOnAction(event -> {
            confirmarSenhaVisivel = !confirmarSenhaVisivel;
            campoConfirmarSenhaVisivel.setVisible(confirmarSenhaVisivel);
            campoConfirmarSenhaVisivel.setManaged(confirmarSenhaVisivel);
            campoConfirmarSenha.setVisible(!confirmarSenhaVisivel);
            campoConfirmarSenha.setManaged(!confirmarSenhaVisivel);

            btnToggleConfirmarSenha.setText(confirmarSenhaVisivel ? "🙈" : "👁");
        });
    }

    @FXML
    protected void voltarParaLogin() throws IOException {
        MainApplication.setRoot("login");
    }

    @FXML
    public void onAlterarSenha() {
        String email = campoEmail.getText();
        String novaSenha = campoNovaSenha.getText(); // O campo oculto continua guardando o valor real corretamente
        String confirmarSenha = campoConfirmarSenha.getText();

        // 1. Validação de campos vazios
        if (email.isBlank() || novaSenha.isBlank() || confirmarSenha.isBlank()) {
            mostrarErro("Campos obrigatórios", "Por favor, preencha todos os campos.");
            return;
        }

        // 2. Validação de domínio institucional
        if (!email.contains("@df.senac.br")) {
            mostrarErro("E-mail Inválido", "Use um e-mail institucional (@df.senac.br).");
            return;
        }

        // 3. Verificação de coincidência de senhas
        if (!novaSenha.equals(confirmarSenha)) {
            mostrarErro("Erro de Senha", "As senhas não coincidem!");
            return;
        }

        // 4. Lógica de Banco de Dados
        AtualizarSenhaDAO dao = new AtualizarSenhaDAO();
        boolean existe = dao.instrutorExiste(email);

        if (existe) {
            try {
                // Criptografa a nova senha antes de salvar
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
        }
        else {
            mostrarErro("E-mail Não Cadastrado", "O e-mail informado não consta em nosso sistema. Verifique os dados.");
            campoEmail.requestFocus();
        }
    }

    /**
     * Método utilitário para exibir alertas de erro
     */
    private void mostrarErro(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void limparCamposFormulario() {
        campoEmail.clear();
        campoNovaSenha.clear();
        campoConfirmarSenha.clear();
    }
}