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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import java.io.File;
import org.githubio.desktop_beleza.model.Servico;
import org.githubio.desktop_beleza.model.ServicosDAO;

import java.net.URL;
import java.util.ResourceBundle;

public class ProcedimentosController implements Initializable {

    @FXML private TableView<Servico> tabelaProcedimentos;
    @FXML private TableColumn<Servico, Integer> colIdProcedimento;
    @FXML private TableColumn<Servico, String> colNomeProcedimento;
    @FXML private TableColumn<Servico, String> colDescricao;
    @FXML private TableColumn<Servico, Void> colAcoes;
    @FXML private TextField txtNomeProcedimento;
    @FXML private TextArea txtDescricao;

    private final ServicosDAO dao = new ServicosDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colIdProcedimento.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomeProcedimento.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colAcoes.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
        configurarColunaAcoes();
        atualizarTabela();
    }

    private void atualizarTabela() {
        tabelaProcedimentos.setItems(FXCollections.observableArrayList(dao.lerTodos()));
    }

    @FXML
    private void salvarProcedimento(ActionEvent event) {
        String nome = txtNomeProcedimento.getText();
        if (nome == null || nome.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Informe o nome do procedimento.");
            alert.showAndWait();
            return;
        }
        String descricao = txtDescricao.getText() == null ? "" : txtDescricao.getText().trim();
        dao.inserir(new Servico(nome.trim(), descricao));
        txtNomeProcedimento.clear();
        txtDescricao.clear();
        atualizarTabela();
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
                    Servico item = getTableRow().getItem();
                    if (item != null) abrirDialogoEdicao(item);
                });
                btnExcluir.setOnAction(e -> {
                    Servico item = getTableRow().getItem();
                    if (item != null) confirmarExclusao(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void abrirDialogoEdicao(Servico procedimento) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Procedimento");
        ButtonType btnSalvar = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        TextField txtNome = new TextField(procedimento.getNome());
        TextField txtDesc = new TextField(procedimento.getDescricao());
        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("Descrição:"), txtDesc));

        dialog.showAndWait().ifPresent(resposta -> {
            if (resposta == btnSalvar && !txtNome.getText().isBlank()) {
                procedimento.setNome(txtNome.getText().trim());
                procedimento.setDescricao(txtDesc.getText().trim());
                dao.atualizar(procedimento);
                atualizarTabela();
            }
        });
    }

    private void confirmarExclusao(Servico procedimento) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Excluir " + procedimento.getNome() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.YES) {
                try {
                    dao.excluir(procedimento.getId());
                    atualizarTabela();
                } catch (RuntimeException e) {
                    Alert erro = new Alert(Alert.AlertType.ERROR);
                    erro.setHeaderText("Não foi possível excluir o procedimento");
                    erro.setContentText(e.getMessage());
                    erro.showAndWait();
                }
            }
        });
    }

    @FXML
    private Button btnAnexarImagem;

    private File imagemSelecionada;

    @FXML
    private void anexarImagem() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar imagem");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File arquivo = fileChooser.showOpenDialog(btnAnexarImagem.getScene().getWindow());
        if (arquivo != null) {
            imagemSelecionada = arquivo;
            btnAnexarImagem.setText(arquivo.getName());
        }
    }
}