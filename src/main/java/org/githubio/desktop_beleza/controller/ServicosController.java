
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

        // 🔑 ESSENCIAL para a coluna de ações
        colAcoes.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));

        configurarColunaAcoes();
        atualizarTabela();
    }

    private void atualizarTabela() {
        tabelaServicos.setItems(FXCollections.observableArrayList(dao.lerTodos()));
    }

    /* ==================================================
       INPUT TIME COMPATÍVEL (Hora + Minuto)
       ================================================== */
    private HBox criarInputHora(int horaInicial, int minutoInicial) {

        Spinner<Integer> spHora = new Spinner<>(0, 23, horaInicial);
        Spinner<Integer> spMinuto = new Spinner<>(0, 59, minutoInicial);

        spHora.setEditable(true);
        spMinuto.setEditable(true);

        spHora.setPrefWidth(70);
        spMinuto.setPrefWidth(70);

        Label separador = new Label(":");

        return new HBox(5, spHora, separador, spMinuto);
    }

    private String obterHorario(HBox box) {
        Spinner<Integer> spHora = (Spinner<Integer>) box.getChildren().get(0);
        Spinner<Integer> spMinuto = (Spinner<Integer>) box.getChildren().get(2);

        return String.format("%02d:%02d",
                spHora.getValue(),
                spMinuto.getValue()
        );
    }

    @FXML
    void abrirDialogoAdicionar() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Novo Serviço");
        dialog.setHeaderText("Preencha os dados do novo serviço");

        ButtonType btnAdicionar = new ButtonType("Adicionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAdicionar, ButtonType.CANCEL);

        TextField txtNome = new TextField();
        TextField txtDesc = new TextField();

        HBox inputHora = criarInputHora(8, 0);

        VBox layout = new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("Descrição:"), txtDesc,
                new Label("Horário:"), inputHora
        );

        dialog.getDialogPane().setContent(layout);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnAdicionar) {

                String horario = obterHorario(inputHora);

                Servico novo = new Servico(
                        txtNome.getText(),
                        txtDesc.getText(),
                        horario
                );

                dao.inserir(novo);
                atualizarTabela();
            }
        });
    }

    /* ==================================================
       COLUNA DE AÇÕES
       ================================================== */
    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<>() {

            private final Button btnEditar = new Button("");
            private final Button btnExcluir = new Button("");
            private final HBox container = new HBox(10, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("editar");
                btnExcluir.getStyleClass().add("excluir");
                container.setAlignment(Pos.CENTER);

                btnEditar.setOnAction(event -> {
                    Servico servico = getTableView().getItems().get(getIndex());
                    abrirDialogoEdicao(servico);
                });

                btnExcluir.setOnAction(event -> {
                    Servico servico = getTableView().getItems().get(getIndex());
                    confirmarExclusao(servico);
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
        dialog.setHeaderText("Altere as informações e clique em Salvar");

        ButtonType btnSalvar = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        TextField txtNome = new TextField(servico.getNome());
        TextField txtDesc = new TextField(servico.getDescricao());

        String[] partes = servico.getHorario().split(":");
        int hora = Integer.parseInt(partes[0]);
        int minuto = Integer.parseInt(partes[1]);

        HBox inputHora = criarInputHora(hora, minuto);

        VBox layout = new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("Descrição:"), txtDesc,
                new Label("Horário:"), inputHora
        );

        dialog.getDialogPane().setContent(layout);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnSalvar) {

                String horario = obterHorario(inputHora);

                servico.setNome(txtNome.getText());
                servico.setDescricao(txtDesc.getText());
                servico.setHorario(horario);

                dao.atualizar(servico);
                tabelaServicos.refresh();
            }
        });
    }

    private void confirmarExclusao(Servico servico) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Excluir Serviço");
        alert.setHeaderText("Deseja excluir: " + servico.getNome() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dao.excluir(servico.getId());
                tabelaServicos.getItems().remove(servico);
            }
        });
    }

    @FXML
    void buscar() {
        String busca = txtBuscar.getText();

        if (busca == null || busca.isEmpty()) {
            atualizarTabela();
        } else {
            tabelaServicos.setItems(FXCollections.observableArrayList(
                    dao.lerTodos().stream()
                            .filter(s -> s.getNome().toLowerCase().contains(busca.toLowerCase()))
                            .toList()
            ));
        }
    }

    @FXML
    void salvar() {
        // Mantido para compatibilidade
    }
}