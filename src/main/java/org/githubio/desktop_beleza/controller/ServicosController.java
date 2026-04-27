package org.githubio.desktop_beleza.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.githubio.desktop_beleza.model.Servico;
import org.githubio.desktop_beleza.model.ServicosDAO;

import java.net.URL;
import java.util.ResourceBundle;

public class ServicosController implements Initializable {

    @FXML private TableView<Servico> tabelaServicos;
    @FXML private TableColumn<Servico, String> colNome;
    @FXML private TableColumn<Servico, String> colDescricao;
    @FXML private TableColumn<Servico, String> colHorario;
    @FXML private TableColumn<Servico, Void> colAcoes;
    @FXML private TextField txtBuscar;

    private final ServicosDAO dao = new ServicosDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));

        colAcoes.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
        configurarColunaAcoes();
        atualizarTabela();
    }

    private void atualizarTabela() {
        tabelaServicos.setItems(
                FXCollections.observableArrayList(dao.lerTodos())
        );
    }

    /* =======================
       INPUT HORA
       ======================= */
    private HBox criarInputHora(int horaInicial, int minutoInicial) {
        Spinner<Integer> spHora = new Spinner<>(0, 23, horaInicial);
        Spinner<Integer> spMinuto = new Spinner<>(0, 59, minutoInicial);

        spHora.setEditable(true);
        spMinuto.setEditable(true);

        spHora.setPrefWidth(70);
        spMinuto.setPrefWidth(70);

        return new HBox(5, spHora, new Label(":"), spMinuto);
    }

    private String obterHorario(HBox box) {
        Spinner<Integer> spHora = (Spinner<Integer>) box.getChildren().get(0);
        Spinner<Integer> spMinuto = (Spinner<Integer>) box.getChildren().get(2);

        return String.format("%02d:%02d",
                spHora.getValue(),
                spMinuto.getValue()
        );
    }

    /* =======================
       BOTÃO ADICIONAR ✅
       ======================= */
    @FXML
    private void abrirDialogoAdicionar(ActionEvent event) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Novo Serviço");
        dialog.setHeaderText("Preencha os dados do serviço");

        ButtonType btnSalvar =
                new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes()
                .addAll(btnSalvar, ButtonType.CANCEL);

        TextField txtNome = new TextField();
        TextField txtDesc = new TextField();

        HBox inputHora = criarInputHora(8, 0);

        VBox layout = new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("Descrição:"), txtDesc,
                new Label("Horário:"), inputHora
        );

        dialog.getDialogPane().setContent(layout);

        dialog.showAndWait().ifPresent(r -> {
            if (r == btnSalvar) {
                Servico novo = new Servico(
                        txtNome.getText(),
                        txtDesc.getText(),
                        obterHorario(inputHora)
                );

                dao.inserir(novo);
                atualizarTabela();
            }
        });
    }

    /* =======================
       COLUNA AÇÕES
       ======================= */
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
                    if (servico != null) {
                        abrirDialogoEdicao(servico);
                    }
                });

                btnExcluir.setOnAction(e -> {
                    Servico servico = getTableRow().getItem();
                    if (servico != null) {
                        confirmarExclusao(servico);
                    }
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

        ButtonType btnSalvar =
                new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes()
                .addAll(btnSalvar, ButtonType.CANCEL);

        TextField txtNome = new TextField(servico.getNome());
        TextField txtDesc = new TextField(servico.getDescricao());

        String[] partes = servico.getHorario().split(":");
        HBox inputHora = criarInputHora(
                Integer.parseInt(partes[0]),
                Integer.parseInt(partes[1])
        );

        dialog.getDialogPane().setContent(
                new VBox(10,
                        new Label("Nome:"), txtNome,
                        new Label("Descrição:"), txtDesc,
                        new Label("Horário:"), inputHora)
        );

        dialog.showAndWait().ifPresent(r -> {
            if (r == btnSalvar) {
                servico.setNome(txtNome.getText());
                servico.setDescricao(txtDesc.getText());
                servico.setHorario(obterHorario(inputHora));

                dao.atualizar(servico);
                tabelaServicos.refresh();
            }
        });
    }

    private void confirmarExclusao(Servico servico) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Excluir " + servico.getNome() + "?");

        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                dao.excluir(servico.getId());
                tabelaServicos.getItems().remove(servico);
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

        tabelaServicos.setItems(
                FXCollections.observableArrayList(
                        dao.lerTodos().stream()
                                .filter(s -> s.getNome().toLowerCase()
                                        .contains(termo.toLowerCase()))
                                .toList()
                )
        );
    }
}
