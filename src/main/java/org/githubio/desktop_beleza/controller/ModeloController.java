package org.githubio.desktop_beleza.controller;


import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.githubio.desktop_beleza.model.Modelo;
import org.githubio.desktop_beleza.model.ModeloDAO;

import java.net.URL;
import java.util.ResourceBundle;

public class ModeloController implements Initializable {

    @FXML private TableView<Modelo> tabelaModelos;
    @FXML private TableColumn<Modelo, String> colNome;
    @FXML private TableColumn<Modelo, String> colTelefone;
    @FXML private TableColumn<Modelo, String> colEmail;
    @FXML private TableColumn<Modelo, Void> colAcoes;
    @FXML private TextField txtBuscar;

    private final ModeloDAO dao = new ModeloDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colAcoes.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
        configurarColunaAcoes();
        atualizarTabela();
    }

    private void atualizarTabela() {
        tabelaModelos.setItems(FXCollections.observableArrayList(dao.lerTodos()));
    }

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("Editar");
            private final Button btnDel = new Button("Excluir");
            private final HBox container = new HBox(10, btnEdit, btnDel);
            {
                btnEdit.setStyle("-fx-background-color: #004587; -fx-text-fill: white;");
                btnDel.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
                container.setAlignment(Pos.CENTER);
                btnEdit.setOnAction(e -> abrirDialogoEdicao(getTableRow().getItem()));
                btnDel.setOnAction(e -> confirmarExclusao(getTableRow().getItem()));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void abrirDialogoEdicao(Modelo modelo) {
        if (modelo == null) return;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Modelo");
        ButtonType btnSalvar = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        TextField txtNome = new TextField(modelo.getNome());
        TextField txtTel = new TextField(modelo.getTelefone());
        TextField txtEmail = new TextField(modelo.getEmail());

        dialog.getDialogPane().setContent(new VBox(10, new Label("Nome:"), txtNome, new Label("Tel:"), txtTel, new Label("Email:"), txtEmail));

        dialog.showAndWait().ifPresent(r -> {
            if (r == btnSalvar) {
                modelo.setNome(txtNome.getText());
                modelo.setTelefone(txtTel.getText());
                modelo.setEmail(txtEmail.getText());
                dao.atualizar(modelo);
                tabelaModelos.refresh();
            }
        });
    }

    private void confirmarExclusao(Modelo modelo) {
        if (modelo == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Excluir " + modelo.getNome() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                dao.excluir(modelo.getId());
                tabelaModelos.getItems().remove(modelo);
            }
        });
    }

    @FXML
    private void abrirTelaAdcionarPopUp() {
        // Exemplo rápido de cadastro direto
        abrirDialogoEdicao(new Modelo()); // Use lógica similar para novo modelo
        atualizarTabela();
    }

    @FXML
    private void buscar() {
        String t = txtBuscar.getText().toLowerCase();
        tabelaModelos.setItems(FXCollections.observableArrayList(
                dao.lerTodos().stream().filter(m -> m.getNome().toLowerCase().contains(t)).toList()
        ));
    }
}
