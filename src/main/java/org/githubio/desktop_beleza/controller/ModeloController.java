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
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.Modelo;
import org.githubio.desktop_beleza.model.ModeloDAO;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ModeloController implements Initializable {

    @FXML private TableView<Modelo> tabelaModelos;
    @FXML private TableColumn<Modelo, String> colNome;
    @FXML private TableColumn<Modelo, String> colTelefone;
    @FXML private TableColumn<Modelo, String> colEmail;
    @FXML private TableColumn<Modelo, Date> colDataNascimento;
    @FXML private TableColumn<Modelo, Void> colAcoes;
    @FXML private TextField txtBuscar;

    private final ModeloDAO dao = new ModeloDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        // CORRIGIDO: o nome da propriedade é "data" (getData()), não "data_nascimento"
        colDataNascimento.setCellValueFactory(new PropertyValueFactory<>("data"));

        colAcoes.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
        configurarColunaAcoes();

        atualizarTabela();
    }

    private void atualizarTabela() {
        tabelaModelos.setItems(
                FXCollections.observableArrayList(dao.lerTodos())
        );
    }

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("");
            private final Button btnDel = new Button("");
            private final HBox container = new HBox(10, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().add("editar");
                btnDel.getStyleClass().add("excluir");
                container.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> abrirDialogoEdicao(getTableRow().getItem()));
                btnDel.setOnAction(e -> confirmarExclusao(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void abrirDialogoEdicao(Modelo modelo) {
        if (modelo == null) return;

        boolean isNovo = (modelo.getId() == 0);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNovo ? "Novo Modelo" : "Editar Modelo");

        ButtonType btnSalvarType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvarType, ButtonType.CANCEL);

        TextField txtNome = new TextField(modelo.getNome());
        TextField txtTel = new TextField(modelo.getTelefone());
        TextField txtEmail = new TextField(modelo.getEmail());
        DatePicker dpNascimento = new DatePicker();

        // CORRIGIDO: preenche o DatePicker com a data já salva (edição)
        if (modelo.getData() != null) {
            dpNascimento.setValue(modelo.getData().toLocalDate());
        }

        TextFormatter<String> nomeFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("[a-zA-ZÀ-ú ]*")) {
                return change;
            }
            return null;
        });
        txtNome.setTextFormatter(nomeFormatter);

        TextFormatter<String> telefoneFormatter = new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d{0,11}")) {
                return change;
            }
            return null;
        });
        txtTel.setTextFormatter(telefoneFormatter);
        txtTel.setPromptText("Somente números (máx. 11)");

        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("Telefone:"), txtTel,
                new Label("Email:"), txtEmail,
                new Label("Data de nascimento:"), dpNascimento
        ));

        final Button btnSalvar = (Button) dialog.getDialogPane().lookupButton(btnSalvarType);

        btnSalvar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String telRaw = txtTel.getText();
            String email = txtEmail.getText();

            // CORRIGIDO: pega o valor real do DatePicker e converte para java.sql.Date
            LocalDate dataEscolhida = dpNascimento.getValue();
            Date dataN = (dataEscolhida != null) ? Date.valueOf(dataEscolhida) : null;

            if (telRaw.length() < 11) {
                mostrarAlerta("Erro no Telefone", "O telefone deve conter 11 dígitos.");
                event.consume();
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                mostrarAlerta("Erro no Email", "Por favor, insira um e-mail válido.");
                event.consume();
                return;
            }

            if (dataN == null) {
                mostrarAlerta("Erro na Data", "Por favor, selecione a data de nascimento.");
                event.consume();
                return;
            }

            modelo.setNome(txtNome.getText());
            modelo.setTelefone(telRaw);
            modelo.setEmail(email);
            modelo.setData(dataN);

            if (isNovo) {
                dao.cadastrar(modelo);
            } else {
                dao.atualizar(modelo);
            }
            atualizarTabela();
        });

        dialog.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void confirmarExclusao(Modelo modelo) {
        if (modelo == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Excluir " + modelo.getNome() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                dao.excluir(modelo.getId());
                atualizarTabela();
            }
        });
    }

    @FXML
    private void abrirTelaAdcionarPopUp() {
        abrirDialogoEdicao(new Modelo());
    }

    @FXML
    private void buscar() {
        String t = txtBuscar.getText().toLowerCase();
        if (t.isEmpty()) {
            atualizarTabela();
        } else {
            tabelaModelos.setItems(FXCollections.observableArrayList(
                    dao.lerTodos().stream()
                            .filter(m -> m.getNome().toLowerCase().contains(t) ||
                                    m.getEmail().toLowerCase().contains(t))
                            .toList()
            ));
        }
    }

    @FXML
    public void trocarTelaParaServicos() throws IOException {
        MainApplication.setRoot("servicos");
    }

    @FXML
    public void trocarTelaParaTurmas() throws IOException {
        MainApplication.setRoot("GerenciarTurma");
    }

    @FXML
    public void trocarTelaParaUnidades() throws IOException {
        MainApplication.setRoot("unidades");
    }

    @FXML
    public void trocarTelaParaPaginaInicial() throws IOException {
        MainApplication.setRoot("Telaagenda");
    }

    @FXML
    public void sairDoSistema() throws IOException {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Sair do Sistema");
        alerta.setHeaderText(null);
        alerta.setContentText("Você deseja do sair do sistema?");

        ButtonType botaoSim = new ButtonType("SIM");
        ButtonType botaoNao = new ButtonType("NÃO");

        alerta.getButtonTypes().setAll(botaoSim, botaoNao);

        if (alerta.showAndWait().get() == botaoSim) {
            MainApplication.setUsuario("");
            MainApplication.setRoot("login");
        }
    }
}