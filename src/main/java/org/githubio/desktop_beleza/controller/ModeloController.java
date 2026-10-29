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
import org.githubio.desktop_beleza.model.dao.ModeloDAO;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        colDataNascimento.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatoBrasileiro = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(Date data, boolean empty) {
                super.updateItem(data, empty);
                setText(empty || data == null ? null : data.toLocalDate().format(formatoBrasileiro));
            }
        });

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
        dialog.getDialogPane().getStyleClass().add("model-dialog");
        var stylesheet = ModeloController.class.getResource("/org/githubio/desktop_beleza/css/style.css");
        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet.toExternalForm());
        }

        ButtonType btnSalvarType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvarType, ButtonType.CANCEL);

        TextField txtNome = new TextField(modelo.getNome());
        TextField txtTel = new TextField(modelo.getTelefone());
        TextField txtEmail = new TextField(modelo.getEmail());
        DatePicker dpNascimento = new DatePicker();
        txtNome.getStyleClass().add("model-dialog-field");
        txtTel.getStyleClass().add("model-dialog-field");
        txtEmail.getStyleClass().add("model-dialog-field");
        dpNascimento.getStyleClass().add("model-dialog-field");
        dpNascimento.setMaxWidth(Double.MAX_VALUE);

        dpNascimento.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate data, boolean vazio) {
                super.updateItem(data, vazio);
                setStyle(!vazio && LocalDate.now().equals(data)
                        ? "-fx-text-fill: #000000;"
                        : "");
            }
        });

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

        VBox formulario = new VBox(12,
                criarCampoFormulario("Nome", txtNome),
                criarCampoFormulario("Telefone", txtTel),
                criarCampoFormulario("Email", txtEmail),
                criarCampoFormulario("Data de nascimento", dpNascimento)
        );
        formulario.getStyleClass().add("model-dialog-form");
        dialog.getDialogPane().setContent(formulario);
        dialog.getDialogPane().setPrefWidth(430);

        final Button btnSalvar = (Button) dialog.getDialogPane().lookupButton(btnSalvarType);
        btnSalvar.getStyleClass().add("panel-button-primary");
        Button btnCancelar = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        btnCancelar.getStyleClass().add("panel-button-secondary");

        btnSalvar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String telRaw = txtTel.getText();
            String email = txtEmail.getText();

            // CORRIGIDO: pega o valor real do DatePicker e converte para java.sql.Date
            LocalDate dataEscolhida = dpNascimento.getValue();
            Date dataN = (dataEscolhida != null) ? Date.valueOf(dataEscolhida) : null;

            if (txtNome.getText().isBlank()
                    || telRaw.isBlank()
                    || email.isBlank()
                    || dataN == null) {
                mostrarAlerta("Campos obrigatórios", "Preencha todos os campos antes de salvar.");
                event.consume();
                return;
            }

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

            try {
                if (isNovo) {
                    dao.cadastrar(modelo);
                } else {
                    dao.atualizar(modelo);
                }
                atualizarTabela();
            } catch (RuntimeException ex) {
                event.consume();
                Throwable causa = ex;
                while (causa.getCause() != null) {
                    causa = causa.getCause();
                }
                String detalhe = causa.getMessage();
                mostrarAlerta("Erro ao salvar modelo",
                        detalhe == null || detalhe.isBlank()
                                ? "Não foi possível salvar o modelo. Confira os dados e a conexão com o banco."
                                : detalhe);
            }
        });

        dialog.showAndWait();
    }

    private VBox criarCampoFormulario(String titulo, Control campo) {
        Label label = new Label(titulo);
        label.getStyleClass().add("model-dialog-label");
        return new VBox(5, label, campo);
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        javafx.stage.Window owner = javafx.stage.Window.getWindows().stream()
                .filter(javafx.stage.Window::isFocused)
                .findFirst()
                .orElse(null);
        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(javafx.stage.Modality.WINDOW_MODAL);
        }
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
                if (dao.excluir(modelo.getId())) {
                    atualizarTabela();
                } else {
                    Alert aviso = new Alert(Alert.AlertType.WARNING);
                    aviso.setTitle("Não foi possível excluir");
                    aviso.setHeaderText(null);
                    aviso.setContentText("Este modelo tem um agendamento e não será possível excluí-lo.");
                    aviso.showAndWait();
                }
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
}
