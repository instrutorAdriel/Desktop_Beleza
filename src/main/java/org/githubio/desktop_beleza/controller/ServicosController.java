package org.githubio.desktop_beleza.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.githubio.desktop_beleza.model.Servico;
import org.githubio.desktop_beleza.model.dao.ServicosDAO;

import java.net.URL;
import java.util.ResourceBundle;

public class ServicosController implements Initializable {

    @FXML private TableView<Servico> tabelaServicos;
    @FXML private TableColumn<Servico, String> colNome;
    @FXML private TableColumn<Servico, String> colDescricao;
    @FXML private TableColumn<Servico, Void> colAcoes;
    @FXML private TextField txtBuscar;

    private final ServicosDAO dao = new ServicosDAO();

    // Cache local em memória
    private final ObservableList<Servico> listaServicosCache = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colAcoes.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
        configurarColunaAcoes();

        // Associa a tabela diretamente ao cache
        tabelaServicos.setItems(listaServicosCache);

        // Carrega do banco de dados APENAS 1 VEZ
        carregarDadosDoBanco();
    }

    private void carregarDadosDoBanco() {
        listaServicosCache.setAll(dao.lerTodos());
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
                Servico novoServico = new Servico(txtNome.getText().trim(), txtDesc.getText().trim());

                // Insere no Banco
                dao.inserir(novoServico);

                // Adiciona direto no Cache da memória (sem ler todos do banco)
                listaServicosCache.add(novoServico);
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

                // Atualiza no Banco
                dao.atualizar(servico);

                // Atualiza na Tabela/Cache
                tabelaServicos.refresh();
            }
        });
    }

    private void confirmarExclusao(Servico servico) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Excluir " + servico.getNome() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.YES) {
                try {
                    // Exclui do Banco
                    dao.excluir(servico.getId());

                    // Remove direto do Cache
                    listaServicosCache.remove(servico);
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
            // Se a busca estiver vazia, restaura o cache original completo
            tabelaServicos.setItems(listaServicosCache);
            return;
        }
        String filtro = termo.toLowerCase();

        // Filtra a partir do CACHE EM MEMÓRIA (sem ir ao banco)
        ObservableList<Servico> listaFiltrada = FXCollections.observableArrayList(
                listaServicosCache.stream()
                        .filter(s -> (s.getNome() != null && s.getNome().toLowerCase().contains(filtro))
                                || (s.getDescricao() != null && s.getDescricao().toLowerCase().contains(filtro)))
                        .toList()
        );

        tabelaServicos.setItems(listaFiltrada);
    }
}