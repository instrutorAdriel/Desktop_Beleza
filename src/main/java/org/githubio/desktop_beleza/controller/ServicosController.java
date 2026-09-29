package org.githubio.desktop_beleza.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.Servico;
import org.githubio.desktop_beleza.model.ServicosDAO;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ServicosController implements Initializable {

    @FXML private TableView<Servico> tabelaServicos;
    @FXML private TableColumn<Servico, String> colNome;
    @FXML private TableColumn<Servico, String> colDescricao;
    @FXML private TableColumn<Servico, Void> colAcoes;
    @FXML private TextField txtBuscar;

    private final ServicosDAO dao = new ServicosDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colAcoes.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
        configurarColunaAcoes();
        atualizarTabela();
    }

    private void atualizarTabela() {
        tabelaServicos.setItems(FXCollections.observableArrayList(dao.lerTodos()));
    }

    @FXML
    private void abrirDialogoAdicionar(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Novo Serviço");
        dialog.setHeaderText("Preencha os dados do serviço/produto");

        ButtonType btnSalvar = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        TextField txtNome = new TextField();
        TextField txtDesc = new TextField();
        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("Descrição:"), txtDesc));

        dialog.showAndWait().ifPresent(resposta -> {
            if (resposta == btnSalvar && !txtNome.getText().isBlank()) {
                dao.inserir(new Servico(txtNome.getText().trim(), txtDesc.getText().trim()));
                atualizarTabela();
            }
        });
    }

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("");
            private final Button btnExcluir = new Button("");
            private final HBox container = new HBox(10, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("editar");
                btnExcluir.getStyleClass().add("excluir");
                container.setAlignment(Pos.CENTER);

                btnEditar.setOnAction(e -> {
                    Servico servico = getTableRow().getItem();
                    if (servico != null) abrirDialogoEdicao(servico);
                });
                btnExcluir.setOnAction(e -> {
                    Servico servico = getTableRow().getItem();
                    if (servico != null) confirmarExclusao(servico);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void abrirDialogoEdicao(Servico servico) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Serviço");
        ButtonType btnSalvar = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        TextField txtNome = new TextField(servico.getNome());
        TextField txtDesc = new TextField(servico.getDescricao());
        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("Descrição:"), txtDesc));

        dialog.showAndWait().ifPresent(resposta -> {
            if (resposta == btnSalvar && !txtNome.getText().isBlank()) {
                servico.setNome(txtNome.getText().trim());
                servico.setDescricao(txtDesc.getText().trim());
                dao.atualizar(servico);
                atualizarTabela();
            }
        });
    }

    private void confirmarExclusao(Servico servico) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Excluir " + servico.getNome() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.YES) {
                try {
                    dao.excluir(servico.getId());
                    atualizarTabela();
                } catch (RuntimeException e) {
                    Alert erro = new Alert(Alert.AlertType.ERROR);
                    erro.setHeaderText("Não foi possível excluir o serviço");
                    erro.setContentText(e.getMessage());
                    erro.showAndWait();
                }
            }
        });
    }

    @FXML
    private void buscar() {
        String termo = txtBuscar.getText();
        if (termo == null || termo.isBlank()) {
            atualizarTabela();
            return;
        }
        String filtro = termo.toLowerCase();
        tabelaServicos.setItems(FXCollections.observableArrayList(
                dao.lerTodos().stream()
                        .filter(s -> s.getNome().toLowerCase().contains(filtro)
                                || (s.getDescricao() != null && s.getDescricao().toLowerCase().contains(filtro)))
                        .toList()));
    }

    @FXML public void trocarTelaParaModelos() throws IOException { MainApplication.setRoot("gerenciarmodelo"); }
    @FXML public void trocarTelaParaTurmas() throws IOException { MainApplication.setRoot("GerenciarTurma"); }
    @FXML public void trocarTelaParaPaginaInicial() throws IOException { MainApplication.setRoot("Telaagenda"); }

    @FXML
    public void sairDoSistema() throws IOException {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Você deseja sair do sistema?", ButtonType.YES, ButtonType.NO);
        if (alerta.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            MainApplication.setUsuario("");
            MainApplication.setRoot("login");
        }
    }
}
