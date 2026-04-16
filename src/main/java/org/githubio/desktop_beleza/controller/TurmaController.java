package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.githubio.desktop_beleza.model.TurmaDAO;


public class TurmaController {

    @FXML private TextField txtNomeTurma;
    @FXML private ChoiceBox<String> comboInstrutor;
    @FXML private Button btnManha, btnTarde, btnNoite;

    private String turnoSelecionado = ""; // Começa vazio
    private TurmaDAO dao = new TurmaDAO();

    @FXML
    public void initialize() {
        // Busca os nomes no banco e joga na ChoiceBox
        java.util.List<String> instrutores = dao.listarNomesInstrutores();
        if (instrutores != null) {
            comboInstrutor.getItems().addAll(instrutores);
        }
    }

    // Métodos OnAction dos botões no Scene Builder
    @FXML
    void selecionarManha() {
        turnoSelecionado = "Matutino";
        destacarBotao(btnManha);
    }

    @FXML
    void selecionarTarde() {
        turnoSelecionado = "Vespertino";
        destacarBotao(btnTarde);
    }

    @FXML
    void selecionarNoite() {
        turnoSelecionado = "Noturno";
        destacarBotao(btnNoite);
    }

    // Função auxiliar para o usuário ver o que selecionou
    private void destacarBotao(Button selecionado) { // Este 'Button' agora é o do JavaFX
        btnManha.setStyle("");
        btnTarde.setStyle("");
        btnNoite.setStyle("");
        selecionado.setStyle("-fx-border-color: blue; -fx-background-color: lightblue;");
    }

    private void mostrarAlerta(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Atenção");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private boolean camposValidos() {
        if (txtNomeTurma.getText().isEmpty()) {
            mostrarAlerta("O nome da turma é obrigatório!");
            return false;
        }
        if (comboInstrutor.getValue() == null) {
            mostrarAlerta("Selecione um instrutor!");
            return false;
        }
        if (turnoSelecionado.isEmpty()) {
            mostrarAlerta("Selecione um turno (Manhã, Tarde ou Noite)!");
            return false;
        }
        return true;
    }

    @FXML
    void finalizarESair() {
        if (camposValidos()) {
            // Adicionamos o quarto parâmetro aqui:
            dao.salvarTurma(txtNomeTurma.getText(), turnoSelecionado, comboInstrutor.getValue(), "Em andamento");

            Stage stage = (Stage) txtNomeTurma.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    void registrarOutra() {
        if (camposValidos()) { // Só entra aqui se tudo estiver preenchido
            dao.salvarTurma(txtNomeTurma.getText(), turnoSelecionado, comboInstrutor.getValue(), "Em andamento");
            // Limpa a tela
            txtNomeTurma.clear();
            comboInstrutor.getSelectionModel().clearSelection();
            turnoSelecionado = "";
            btnManha.setStyle("");
            btnTarde.setStyle("");
            btnNoite.setStyle("");
        }
    }
}