
package org.githubio.desktop_beleza.controller;

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
        // 1. Vincular colunas aos atributos do modelo
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));

        // 2. Configurar botões de ação (Editar/Excluir)
        configurarColunaAcoes();

        // 3. Carregar dados iniciais do banco
        atualizarTabela();
    }

    private void atualizarTabela() {
        tabelaServicos.setItems(FXCollections.observableArrayList(dao.lerTodos()));
    }

    @FXML
    void abrirDialogoAdicionar() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Novo Serviço");
        dialog.setHeaderText("Preencha os dados do novo serviço");

        ButtonType btnAdicionar = new ButtonType("Adicionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAdicionar, ButtonType.CANCEL);

        // Correção: No campo de escolher a hora, desenvolva um código que a pessoa selecione o horário separadamente
        // um campo para a pessoa digitar o horário não é prático.
        TextField txtNome = new TextField();
        TextField txtDesc = new TextField();
        TextField txtHora = new TextField();
        txtHora.setPromptText("Ex: 08:00 - 12:00");

        VBox layout = new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("Descrição:"), txtDesc,
                new Label("Horário:"), txtHora
        );
        dialog.getDialogPane().setContent(layout);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnAdicionar) {
                Servico novo = new Servico(txtNome.getText(), txtDesc.getText(), txtHora.getText());
                dao.inserir(novo); // Insere no banco
                atualizarTabela(); // Atualiza a lista na tela
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

    // Correção: Metodo redundante, você criou o mesmo metodo do abrirDialogo. Crie o código que tenha apenas um metodo
    // de abrir o dialogo
    private void abrirDialogoEdicao(Servico servico) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Serviço");
        dialog.setHeaderText("Altere as informações e clique em Salvar");

        ButtonType btnSalvarTipo = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvarTipo, ButtonType.CANCEL);

        TextField txtNome = new TextField(servico.getNome());
        TextField txtDesc = new TextField(servico.getDescricao());
        TextField txtHora = new TextField(servico.getHorario());

        VBox layout = new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("Descrição:"), txtDesc,
                new Label("Horário:"), txtHora
        );
        dialog.getDialogPane().setContent(layout);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnSalvarTipo) {
                servico.setNome(txtNome.getText());
                servico.setDescricao(txtDesc.getText());
                servico.setHorario(txtHora.getText());
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

    @FXML void salvar() { /* Método mantido para compatibilidade */ }
}