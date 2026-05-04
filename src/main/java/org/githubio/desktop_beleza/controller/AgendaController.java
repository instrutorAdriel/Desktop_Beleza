package org.githubio.desktop_beleza.controller;

import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.Agenda;
import org.githubio.desktop_beleza.model.AgendaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AgendaController {

    @FXML private ComboBox<String> txtServico;
    @FXML private DatePicker       dpData;
    @FXML private TextField        txtCliente;
    @FXML private TextField        txtHorario;
    @FXML private ComboBox<String> cbTurma;
    @FXML private TableView<Agenda>           tabelaAgenda;
    @FXML private TableColumn<Agenda, String> colData;
    @FXML private TableColumn<Agenda, String> colServico;
    @FXML private TableColumn<Agenda, String> colCliente;
    @FXML private TableColumn<Agenda, String> colHorario;
    @FXML private TableColumn<Agenda, String> colStatus;
    @FXML private TableColumn<Agenda, String> colAcao;
    @FXML private Label  lblSemanaAtual;
    @FXML private Button btnVerTodos;

    private Agenda    agendaSendoEditada = null;
    private LocalDate inicioSemanaAtual;
    private boolean   exibindoTodos     = false;
    private final Map<String, Integer> mapaTurmas = new HashMap<>();

    @FXML
    public void initialize() {

        dpData.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #d3d3d3;");
                }
            }
        });

        dpData.valueProperty().addListener((obs, dataAntiga, dataSelecionada) -> {
            if (dataSelecionada != null) {
                exibindoTodos = false;
                btnVerTodos.setText("Ver todos");
                inicioSemanaAtual = dataSelecionada.with(DayOfWeek.MONDAY);
                atualizarTabela();
            }
        });

        List<String> servicos = new AgendaDAO().listarServicos();
        txtServico.getItems().addAll(servicos);

        // ── Validação de horário em tempo real ────────────────────────────────
        TextFormatter<String> horarioFormatter = new TextFormatter<>(change -> {
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
        txtHorario.setTextFormatter(horarioFormatter);
        txtHorario.setPromptText("HH:mm");
        // ─────────────────────────────────────────────────────────────────────

        carregarTurmas();

        cbTurma.valueProperty().addListener((obs, antiga, nova) -> {
            if (nova != null) {
                exibindoTodos = false;
                btnVerTodos.setText("Ver todos");
                atualizarTabela();
            }
        });

        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colServico.setCellValueFactory(new PropertyValueFactory<>("servico"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tabelaAgenda.getColumns().addListener(new ListChangeListener<TableColumn<Agenda, ?>>() {
            private boolean suspender = false;
            @Override
            public void onChanged(Change<? extends TableColumn<Agenda, ?>> c) {
                while (c.next()) {
                    if (!suspender && (c.wasReplaced() || c.wasAdded() || c.wasRemoved())) {
                        suspender = true;
                        tabelaAgenda.getColumns().setAll(
                                colData, colServico, colCliente, colHorario, colStatus, colAcao);
                        suspender = false;
                    }
                }
            }
        });

        tabelaAgenda.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colStatus.setCellFactory(col -> new TableCell<Agenda, String>() {
            private final MenuItem   itemPendente      = new MenuItem("Pendente");
            private final MenuItem   itemCompareceu    = new MenuItem("Compareceu");
            private final MenuItem   itemNaoCompareceu = new MenuItem("Não Compareceu");
            private final MenuButton mnuOpcoes         = new MenuButton("Status", null,
                    itemPendente, itemCompareceu, itemNaoCompareceu);

            {
                itemPendente.setOnAction(e -> {
                    Agenda item = getTableView().getItems().get(getIndex());
                    item.setStatus("Pendente");
                    mnuOpcoes.setText("Pendente");
                    new AgendaDAO().atualizarStatus(item.getId(), "Pendente");
                });
                itemCompareceu.setOnAction(e -> {
                    Agenda item = getTableView().getItems().get(getIndex());
                    item.setStatus("Compareceu");
                    mnuOpcoes.setText("Compareceu");
                    new AgendaDAO().atualizarStatus(item.getId(), "Compareceu");
                });
                itemNaoCompareceu.setOnAction(e -> {
                    Agenda item = getTableView().getItems().get(getIndex());
                    item.setStatus("Não Compareceu");
                    mnuOpcoes.setText("Não Compareceu");
                    new AgendaDAO().atualizarStatus(item.getId(), "Não Compareceu");
                });
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    mnuOpcoes.setText(status);
                    setGraphic(mnuOpcoes);
                }
            }
        });

        colAcao.setCellFactory(parm -> new TableCell<>() {
            private final MenuButton mnuOpcoes = new MenuButton("...");
            private final MenuItem   itemEdit  = new MenuItem("Editar");
            private final MenuItem   itemDel   = new MenuItem("Excluir");

            {
                mnuOpcoes.getItems().addAll(itemEdit, itemDel);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Agenda agendaDaLinha = getTableView().getItems().get(getIndex());

                    itemDel.setOnAction(e -> {
                        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
                        alerta.setTitle("Excluir Agendamento");
                        alerta.setHeaderText("Deseja excluir o agendamento de "
                                + agendaDaLinha.getCliente() + "?");
                        if (alerta.showAndWait().get() == ButtonType.OK) {
                            new AgendaDAO().excluirAgendamento(agendaDaLinha.getId());
                            atualizarTabela();
                        }
                    });

                    itemEdit.setOnAction(e -> {
                        txtCliente.setText(agendaDaLinha.getCliente());
                        txtServico.setValue(agendaDaLinha.getServico());
                        txtHorario.setText(agendaDaLinha.getHorario());
                        agendaSendoEditada = agendaDaLinha;
                    });

                    setGraphic(mnuOpcoes);
                }
            }
        });

        inicioSemanaAtual = LocalDate.now().with(DayOfWeek.MONDAY);
        atualizarTabela();
    }

    private void carregarTurmas() {
        String email = MainApplication.getUsuario();
        List<String[]> turmas = new AgendaDAO().listarTurmasDoInstrutor(email);

        mapaTurmas.clear();
        cbTurma.getItems().clear();

        for (String[] turma : turmas) {
            int    id    = Integer.parseInt(turma[0]);
            String label = turma[1];
            mapaTurmas.put(label, id);
            cbTurma.getItems().add(label);
        }

        if (!cbTurma.getItems().isEmpty()) {
            cbTurma.getSelectionModel().selectFirst();
        }
    }

    @FXML
    protected void onVerTodosClick() {
        exibindoTodos = !exibindoTodos;

        String email            = MainApplication.getUsuario();
        String turmaSelecionada = cbTurma.getValue();

        if (exibindoTodos) {
            btnVerTodos.setText("Ver semana");
            if (lblSemanaAtual != null)
                lblSemanaAtual.setText("Exibindo todos os agendamentos");

            if (turmaSelecionada != null) {
                int idTurma = mapaTurmas.get(turmaSelecionada);
                tabelaAgenda.setItems(FXCollections.observableArrayList(
                        new AgendaDAO().listarAgendamentosPorTurma(email, idTurma)
                ));
            }
        } else {
            btnVerTodos.setText("Ver todos");
            atualizarTabela();
        }
    }

    @FXML
    protected void onSalvarButtonClick() {
        if (txtServico.getValue() == null
                || dpData.getValue() == null
                || txtCliente.getText().isBlank()
                || txtHorario.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Preencha todos os campos!");
            alert.show();
            return;
        }

        // ── Valida formato HH:mm completo antes de salvar ─────────────────────
        String horario = txtHorario.getText();
        if (!horario.matches("([01][0-9]|2[0-3]):[0-5][0-9]")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Horário inválido");
            alert.setContentText("Informe o horário no formato HH:mm (ex: 09:30)");
            alert.show();
            return;
        }
        // ─────────────────────────────────────────────────────────────────────

        String data    = String.valueOf(dpData.getValue());
        String cliente = txtCliente.getText();
        String servico = txtServico.getValue();

        AgendaDAO dao = new AgendaDAO();

        if (agendaSendoEditada != null) {
            agendaSendoEditada.setData(data);
            agendaSendoEditada.setServico(servico);
            agendaSendoEditada.setCliente(cliente);
            agendaSendoEditada.setHorario(horario);
            dao.editarAgendamento(agendaSendoEditada);
            agendaSendoEditada = null;

        } else {
            int idModelo = dao.cadastrarERetornarIdModelo(cliente);
            if (idModelo == -1) {
                new Alert(Alert.AlertType.ERROR, "Erro ao cadastrar cliente!").show();
                return;
            }

            int idServico = dao.cadastrarERetornarIdServico(servico);
            if (idServico == -1) {
                new Alert(Alert.AlertType.ERROR, "Serviço não encontrado!").show();
                return;
            }

            String turmaSelecionada = cbTurma.getValue();
            if (turmaSelecionada == null) {
                new Alert(Alert.AlertType.ERROR, "Selecione uma turma!").show();
                return;
            }
            int idTurmasInstrutores = mapaTurmas.get(turmaSelecionada);

            dao.cadastrarAgendamento(data, horario, 1, idModelo, idServico, idTurmasInstrutores);
        }

        limparCampos();
        atualizarTabela();
    }

    private void limparCampos() {
        txtCliente.clear();
        txtHorario.clear();
        dpData.setValue(null);
        txtServico.setValue(null);
    }

    public void atualizarTabela() {
        LocalDate fimSemana = inicioSemanaAtual.plusDays(6);

        if (lblSemanaAtual != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                    "EEEE, dd/MM/yyyy", new Locale("pt", "BR"));
            lblSemanaAtual.setText(
                    "Início: " + inicioSemanaAtual.format(fmt) + "\n" +
                            "Fim: "    + fimSemana.format(fmt));
        }

        String email            = MainApplication.getUsuario();
        String turmaSelecionada = cbTurma.getValue();

        if (turmaSelecionada == null) {
            tabelaAgenda.setItems(FXCollections.observableArrayList());
            return;
        }

        int idTurma = mapaTurmas.get(turmaSelecionada);

        List<Agenda> lista = new AgendaDAO()
                .listarAgendamentosPorSemanaETurma(inicioSemanaAtual, fimSemana, email, idTurma);
        tabelaAgenda.setItems(FXCollections.observableArrayList(lista));
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
    public void trocarTelaParaServicos() throws IOException {
        MainApplication.setRoot("servico");
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