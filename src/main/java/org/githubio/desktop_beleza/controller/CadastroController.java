package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.CadastroDAO;

import java.io.IOException;
import java.util.regex.Pattern;

public class CadastroController {
    @FXML private TextField campoNome;
    @FXML private TextField campoEmail;
    @FXML private PasswordField campoSenha;
    @FXML private PasswordField campoConfirmarSenha;

    @FXML
    protected void onVoltarClick() throws IOException {
        MainApplication.setRoot("login");
    }

    @FXML
    public void onCadastrarClick() throws IOException {
        String nome = campoNome.getText().trim();
        String email = campoEmail.getText().trim();
        String senha = campoSenha.getText();
        String confirma = campoConfirmarSenha.getText();

        if (nome.isBlank() || email.isBlank() || senha.isBlank() || confirma.isBlank()) {
            mostrarErro("Campos obrigatórios", "Preencha todos os campos.");
            return;
        }

        Pattern emailInstitucional = Pattern.compile("^[a-zA-Z0-9._%+-]+@df\\.senac\\.br$", Pattern.CASE_INSENSITIVE);
        if (!emailInstitucional.matcher(email).matches()) {
            mostrarErro("E-mail fora do padrão", "Use um e-mail institucional @df.senac.br.");
            return;
        }

        if (!senha.equals(confirma)) {
            mostrarErro("Erro de senha", "As senhas não coincidem.");
            return;
        }

        CadastroDAO dao = new CadastroDAO();
        if (dao.instrutorExiste(email)) {
            mostrarErro("E-mail já cadastrado", "Este instrutor já está cadastrado.");
            return;
        }

        try {
            dao.cadastrarUsuario(nome, email, senha);
        } catch (RuntimeException e) {
            mostrarErro("Erro no cadastro", e.getMessage());
            return;
        }

        Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
        sucesso.setHeaderText(null);
        sucesso.setContentText("Instrutor cadastrado com sucesso!");
        sucesso.showAndWait();
        MainApplication.setRoot("login");
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
