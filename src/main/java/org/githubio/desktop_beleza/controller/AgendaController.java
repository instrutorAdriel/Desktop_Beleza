package org.githubio.desktop_beleza.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.Agenda;
import org.githubio.desktop_beleza.model.AgendaDAO;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AgendaController {

    @FXML private ComboBox<String> txtServico;
    @FXML private DatePicker dpData;
    @FXML private ComboBox<String> txtModelo;
    @FXML private TextField txtHorario;
    @FXML private ComboBox<String> cbUnidade;
    @FXML private TableView<Agenda> tabelaAgenda;
    @FXML private TableColumn<Agenda, String> colData;
    @FXML private TableColumn<Agenda, String> colServico;
    @FXML private TableColumn<Agenda, String> colModelo;
    @FXML private TableColumn<Agenda, String> colHorario;
    @FXML private TableColumn<Agenda, String> colStatus;
    @FXML private TableColumn<Agenda, String> colAcao;
    @FXML private Label lblSemanaAtual;
    @FXML private Button btnVerTodos;

    private final AgendaDAO dao = new AgendaDAO();
    private LocalDate inicioSemanaAtual = LocalDate.now().with(DayOfWeek.MONDAY);
    private boolean exibindoTodos = false;

    @FXML
    public void initialize() {
        txtServico.getItems().setAll(dao.listarServicos());
        txtModelo.getItems().setAll(dao.listarModelos());
        cbUnidade.getItems().setAll(dao.listarUnidades());
        if (!cbUnidade.getItems().isEmpty()) {
            cbUnidade.getSelectionModel().selectFirst();
        }

        btnVerTodos.setText("Ver todos");

        dpData.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (!empty && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;");
                }
            }
        });

        dpData.valueProperty().addListener((obs, antiga, nova) -> {
            if (nova != null && !exibindoTodos) {
                inicioSemanaAtual = nova.with(DayOfWeek.MONDAY);
                atualizarTabela();
            }
        });

        txtHorario.setTextFormatter(criarFormatadorHorario());
        txtHorario.setPromptText("HH:mm");

        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colServico.setCellValueFactory(new PropertyValueFactory<>("servico"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        configurarStatus();
        configurarAcoes();
        tabelaAgenda.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        atualizarTabela();
    }

    private TextFormatter<String> criarFormatadorHorario() {
        return new TextFormatter<>(change -> {
            String novo = change.getControlNewText();
            if (novo.matches("([01]?[0-9]?|2[0-3]?|([01][0-9]|2[0-3]):[0-5]?[0-9]?)")) {
                if (change.getText().matches("[0-9]") && novo.length() == 2 && !novo.contains(":")) {
                    change.setText(change.getText() + ":");
                    change.setCaretPosition(change.getCaretPosition() + 1);
                    change.setAnchor(change.getAnchor() + 1);
                }
                return change;
            }
            return null;
        });
    }

    private void configurarStatus() {
        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final MenuButton menu = new MenuButton("Status");

            {
                String[] statusPermitidos = {
                        "Confirmado", "Pendente", "Realizado", "Cancelado", "Não Compareceu"
                };
                for (String status : statusPermitidos) {
                    MenuItem item = new MenuItem(status);
                    item.setOnAction(e -> {
                        Agenda agenda = getTableView().getItems().get(getIndex());
                        dao.atualizarStatus(agenda.getId(), status);
                        agenda.setStatus(status);
                        menu.setText(status);
                    });
                    menu.getItems().add(item);
                }
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    menu.setText(status);
                    setGraphic(menu);
                }
            }
        });
    }

    private void configurarAcoes() {
        colAcao.setCellFactory(coluna -> new TableCell<>() {
            private final Button btnEditar = new Button("");
            private final Button btnExcluir = new Button("");
            private final HBox caixa = new HBox(10, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("editar");
                btnExcluir.getStyleClass().add("excluir");
                caixa.setAlignment(Pos.CENTER);

                btnEditar.setOnAction(e -> {
                    Agenda agenda = getTableView().getItems().get(getIndex());
                    abrirPopupEdicao(agenda);
                });

                btnExcluir.setOnAction(e -> {
                    Agenda agenda = getTableView().getItems().get(getIndex());
                    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                            "Excluir o agendamento de " + agenda.getModelo() + "?",
                            ButtonType.YES, ButtonType.NO);
                    alerta.showAndWait().ifPresent(resposta -> {
                        if (resposta == ButtonType.YES) {
                            dao.excluirAgendamento(agenda.getId());
                            atualizarTabela();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : caixa);
            }
        });
    }

    private void abrirPopupEdicao(Agenda agenda) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Editar Agendamento");

        ComboBox<String> campoModelo = new ComboBox<>();
        campoModelo.getItems().setAll(txtModelo.getItems());
        campoModelo.setValue(agenda.getModelo());

        ComboBox<String> campoServico = new ComboBox<>();
        campoServico.getItems().setAll(txtServico.getItems());
        campoServico.setValue(agenda.getServico());

        TextField campoHorario = new TextField(agenda.getHorario());
        campoHorario.setTextFormatter(criarFormatadorHorario());

        DatePicker campoData = new DatePicker(LocalDate.parse(agenda.getData()));
        campoData.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (!empty && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Modelo:"), 0, 0);
        grid.add(campoModelo, 1, 0);
        grid.add(new Label("Serviço:"), 0, 1);
        grid.add(campoServico, 1, 1);
        grid.add(new Label("Horário:"), 0, 2);
        grid.add(campoHorario, 1, 2);
        grid.add(new Label("Data:"), 0, 3);
        grid.add(campoData, 1, 3);

        Button btnSalvar = new Button("Salvar");
        Button btnCancelar = new Button("Cancelar");

        btnSalvar.setOnAction(e -> {
            if (campoModelo.getValue() == null || campoServico.getValue() == null ||
                    campoData.getValue() == null || !horarioValido(campoHorario.getText())) {
                mostrarAviso("Preencha os campos e informe o horário no formato HH:mm.");
                return;
            }

            agenda.setModelo(campoModelo.getValue());
            agenda.setServico(campoServico.getValue());
            agenda.setData(campoData.getValue().toString());
            agenda.setHorario(campoHorario.getText());
            dao.editarAgendamento(agenda);
            popup.close();
            atualizarTabela();
        });
        btnCancelar.setOnAction(e -> popup.close());

        HBox botoes = new HBox(10, btnSalvar, btnCancelar);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setPadding(new Insets(0, 20, 15, 0));

        popup.setScene(new Scene(new VBox(10, grid, botoes), 380, 280));
        popup.setResizable(false);
        popup.showAndWait();
    }

    @FXML
    protected void onVerTodosClick() {
        exibindoTodos = !exibindoTodos;
        btnVerTodos.setText(exibindoTodos ? "Ver semana" : "Ver todos");
        atualizarTabela();
    }

    @FXML
    protected void onSalvarButtonClick() {
        if (txtServico.getValue() == null || dpData.getValue() == null ||
                txtModelo.getValue() == null || cbUnidade.getValue() == null ||
                !horarioValido(txtHorario.getText())) {
            mostrarAviso("Preencha todos os campos. O horário deve estar no formato HH:mm.");
            return;
        }

        try {
            dao.cadastrarAgendamento(
                    dpData.getValue().toString(),
                    txtHorario.getText(),
                    txtModelo.getValue(),
                    txtServico.getValue(),
                    cbUnidade.getValue(),
                    MainApplication.getUsuario()
            );
            inicioSemanaAtual = dpData.getValue().with(DayOfWeek.MONDAY);
            exibindoTodos = false;
            btnVerTodos.setText("Ver todos");
            limparCampos();
            atualizarTabela();
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível salvar o agendamento", e.getMessage());
        }
    }

    private boolean horarioValido(String horario) {
        return horario != null && horario.matches("([01][0-9]|2[0-3]):[0-5][0-9]");
    }

    private void limparCampos() {
        txtModelo.setValue(null);
        txtHorario.clear();
        dpData.setValue(null);
        txtServico.setValue(null);
    }

    public void atualizarTabela() {
        List<Agenda> lista;
        if (exibindoTodos) {
            lista = dao.listarAgendamentos(MainApplication.getUsuario());
            if (lblSemanaAtual != null) {
                lblSemanaAtual.setText("Todos os agendamentos");
            }
        } else {
            LocalDate fimSemana = inicioSemanaAtual.plusDays(6);
            lista = dao.listarAgendamentosPorSemana(inicioSemanaAtual, fimSemana, MainApplication.getUsuario());
            if (lblSemanaAtual != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));
                lblSemanaAtual.setText("Semana: " + inicioSemanaAtual.format(fmt) + " a " + fimSemana.format(fmt));
            }
        }
        tabelaAgenda.setItems(FXCollections.observableArrayList(lista));
    }

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    @FXML
    public void trocarTelaParaModelos() throws IOException {
        MainApplication.setRoot("gerenciarmodelo");
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
    public void trocarTelaParaServicos() throws IOException {
        MainApplication.setRoot("servicos");
    }

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
