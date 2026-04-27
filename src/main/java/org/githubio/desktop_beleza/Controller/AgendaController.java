package org.githubio.desktop_beleza.controller;

import org.githubio.desktop_beleza.model.Agenda;
import org.githubio.desktop_beleza.model.AgendaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AgendaController {
    @FXML private ComboBox<String> txtServico;
    @FXML private DatePicker dpData;
    @FXML private TextField txtCliente;
    @FXML private TextField txtHorario;
    @FXML private TableView<Agenda> tabelaAgenda;
    @FXML private TableColumn<Agenda, String> colData;
    @FXML private TableColumn<Agenda, String> colServico;
    @FXML private TableColumn<Agenda, String> colCliente;
    @FXML private TableColumn<Agenda, String> colHorario;
    @FXML private TableColumn<Agenda, String> colStatus;
    @FXML private TableColumn<Agenda, String> colAcao;
    @FXML private Label lblSemanaAtual;
    @FXML private Button btnVerTodos;

    private Agenda agendaSendoEditada = null;
    private LocalDate inicioSemanaAtual;
    private boolean exibindoTodos = false;

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
                        tabelaAgenda.getColumns().setAll(colData, colServico, colCliente, colHorario, colStatus, colAcao);
                        suspender = false;
                    }
                }
            }
        });

        tabelaAgenda.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colStatus.setCellFactory(col -> new TableCell<Agenda, String>() {
            private final MenuItem itemPendente      = new MenuItem("Pendente");
            private final MenuItem itemCompareceu    = new MenuItem("Compareceu");
            private final MenuItem itemNaoCompareceu = new MenuItem("Não Compareceu");
            private final MenuButton mnuOpcoes       = new MenuButton("Status", null,
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
            private final MenuItem itemEdit    = new MenuItem("Editar");
            private final MenuItem itemDel     = new MenuItem("Excluir");

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
                        alerta.setHeaderText("Deseja excluir o agendamento de " + agendaDaLinha.getCliente() + "?");
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

    @FXML
    protected void onVerTodosClick() {
        exibindoTodos = !exibindoTodos;

        if (exibindoTodos) {
            btnVerTodos.setText("Ver semana");
            if (lblSemanaAtual != null) lblSemanaAtual.setText("Exibindo todos os agendamentos");
            tabelaAgenda.setItems(FXCollections.observableArrayList(new AgendaDAO().listarAgendamentos()));
        } else {
            btnVerTodos.setText("Ver todos");
            atualizarTabela();
        }
    }

    @FXML
    protected void onSalvarButtonClick() {
        if (txtServico.getValue() == null || dpData.getValue() == null ||
                txtCliente.getText().isBlank() || txtHorario.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Preencha todos os campos!");
            alert.show();
            return;
        }

        String data    = String.valueOf(dpData.getValue());
        String horario = txtHorario.getText();
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
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Erro ao cadastrar cliente!");
                alert.show();
                return;
            }

            int idServico = dao.cadastrarERetornarIdServico(servico);
            if (idServico == -1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Serviço não encontrado!");
                alert.show();
                return;
            }

            dao.cadastrarAgendamento(data, horario, 1, idModelo, idServico, 1);
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
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("pt", "BR"));
            lblSemanaAtual.setText(
                    "Início: " + inicioSemanaAtual.format(fmt) + "\n" +
                            "Fim: "    + fimSemana.format(fmt)
            );
        }

        List<Agenda> lista = new AgendaDAO()
                .listarAgendamentosPorSemana(inicioSemanaAtual, fimSemana);
        tabelaAgenda.setItems(FXCollections.observableArrayList(lista));
    }
}