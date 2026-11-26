package org.githubio.desktop_beleza.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.Agendamento;
import org.githubio.desktop_beleza.model.Modelo;
import org.githubio.desktop_beleza.model.Produto;
import org.githubio.desktop_beleza.model.dao.AgendaDAO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AgendaController {

    private static final DateTimeFormatter FMT_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String TODAS_UNIDADES = "Todas as unidades";
    private static final String TODOS_SERVICOS = "Todos os procedimentos";

    @FXML private ComboBox<String> txtServico;
    @FXML private DatePicker dpData;
    @FXML private ComboBox<String> cbUnidade;
    @FXML private TableView<Agendamento> tabelaAgenda;
    @FXML private TableColumn<Agendamento, String> colData;
    @FXML private TableColumn<Agendamento, String> colServico;
    @FXML private TableColumn<Agendamento, String> colModelo;
    @FXML private TableColumn<Agendamento, String> colHorario;
    @FXML private TableColumn<Agendamento, String> colStatus;
    @FXML private TableColumn<Agendamento, String> colAcao;
    @FXML private Label lblSemanaAtual;
    @FXML private Label lblPraticasDoMes;
    @FXML private Label lblVagasOcupadas;
    @FXML private Label lblTurmasAtivas;

    private final AgendaDAO dao = new AgendaDAO();

    @FXML
    public void initialize() {

        txtServico.getItems().add(TODOS_SERVICOS);
        txtServico.getItems().addAll(dao.listarProdutos());
        txtServico.getSelectionModel().selectFirst();

        cbUnidade.getItems().add(TODAS_UNIDADES);
        cbUnidade.getItems().addAll(dao.listarUnidades());
        cbUnidade.getSelectionModel().selectFirst();

        dpData.setConverter(criarConversorData());
        dpData.setPromptText("dd/mm/aaaa");

        colData.setCellValueFactory(new PropertyValueFactory<>("dataHoraAgendamentoFormatada"));
        colServico.setCellValueFactory(new PropertyValueFactory<>("servico"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        configurarStatus();
        configurarAcoes();
        tabelaAgenda.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        atualizarTabela();

    }

    private StringConverter<LocalDate> criarConversorData() {
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate d) {
                return d == null ? "" : FMT_BR.format(d);
            }

            @Override
            public LocalDate fromString(String s) {
                if (s == null || s.isBlank()) return null;
                try {
                    return LocalDate.parse(s.trim(), FMT_BR);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        };
    }

    private String paraDataBr(String iso) {
        try {
            return LocalDate.parse(iso).format(FMT_BR);
        } catch (Exception e) {
            return iso;
        }
    }

    // ---------- Busca ----------
    @FXML
    protected void onBuscarClick() {
        atualizarTabela();
    }

    public void atualizarTabela() {
        LocalDate data = dpData.getValue();
        String unidade = TODAS_UNIDADES.equals(cbUnidade.getValue()) ? null : cbUnidade.getValue();
        String servico = TODOS_SERVICOS.equals(txtServico.getValue()) ? null : txtServico.getValue();

        List<Agendamento> lista = dao.buscarAgendamentos(data, unidade, servico);
        tabelaAgenda.setItems(FXCollections.observableArrayList(lista));

        if (lblSemanaAtual != null) {
            lblSemanaAtual.setText(data == null
                    ? "Todos os agendamentos"
                    : "Agendamentos de " + data.format(FMT_BR));
        }
        atualizarVisaoGeral();
    }
    @FXML
    protected void onLimparFiltroClick() {
        dpData.setValue(null);
        dpData.getEditor().clear();
        txtServico.getSelectionModel().selectFirst(); // "Todos os procedimentos"
        cbUnidade.getSelectionModel().selectFirst();  // "Todas as unidades"
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

    private boolean horarioValido(String horario) {
        return horario != null && horario.matches("([01][0-9]|2[0-3]):[0-5][0-9]");
    }

    private void configurarStatus() {
        colStatus.setCellFactory(coluna -> new TableCell<>() {
            private final MenuButton menu = new MenuButton("Status");

            {
                String[] statusPermitidos = {
                        "Confirmado", "Realizado", "Cancelado", "Não Compareceu"
                };
                for (String status : statusPermitidos) {
                    MenuItem item = new MenuItem(status);
                    item.setOnAction(e -> {
                        Agendamento agendamento = getTableView().getItems().get(getIndex());
                        dao.atualizarStatus(agendamento.getId(), status);
                        agendamento.setStatus(status);
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
                    Agendamento agendamento = getTableView().getItems().get(getIndex());
                    abrirPopupEdicao(agendamento);
                });

                btnExcluir.setOnAction(e -> {
                    Agendamento agendamento = getTableView().getItems().get(getIndex());
                    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                            "Excluir o agendamento de " + agendamento.getModelo() + "?",
                            ButtonType.YES, ButtonType.NO);
                    alerta.showAndWait().ifPresent(resposta -> {
                        if (resposta == ButtonType.YES) {
                            dao.excluirAgendamento(agendamento.getId());
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

    private void abrirPopupEdicao(Agendamento agendamento) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Editar Agendamento");

        ComboBox<Modelo> campoModelo = new ComboBox<>();
        campoModelo.getItems().setAll(dao.listarModelos());
        campoModelo.setValue(agendamento.getModelo());

        ComboBox<Produto> campoProduto = new ComboBox<>();
        campoProduto.getItems().setAll(dao.listarProdutos());
        campoProduto.setValue(agendamento.getProduto());

        TextField campoHorario = new TextField(agendamento.getHorario());
        campoHorario.setTextFormatter(criarFormatadorHorario());

        DatePicker campoData = new DatePicker(LocalDate.parse(agendamento.getData_hora_agendamento()));
        campoData.setConverter(criarConversorData());
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
        grid.getStyleClass().add("edicao-agendamento");
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        ColumnConstraints colunaRotulos = new ColumnConstraints();
        colunaRotulos.setMinWidth(75);
        colunaRotulos.setPrefWidth(75);
        ColumnConstraints colunaCampos = new ColumnConstraints();
        colunaCampos.setHgrow(Priority.ALWAYS);
        colunaCampos.setFillWidth(true);
        grid.getColumnConstraints().setAll(colunaRotulos, colunaCampos);

        Label rotuloModelo = new Label("Modelo:");
        Label rotuloServico = new Label("Procedimento:");
        Label rotuloHorario = new Label("Horário:");
        Label rotuloData = new Label("Data:");
        for (Label rotulo : List.of(rotuloModelo, rotuloServico, rotuloHorario, rotuloData)) {
            rotulo.setMinWidth(75);
        }
        campoModelo.setMaxWidth(Double.MAX_VALUE);
        campoProduto.setMaxWidth(Double.MAX_VALUE);
        campoHorario.setMaxWidth(Double.MAX_VALUE);
        campoData.setMaxWidth(Double.MAX_VALUE);

        grid.add(rotuloModelo, 0, 0);
        grid.add(campoModelo, 1, 0);
        grid.add(rotuloServico, 0, 1);
        grid.add(campoProduto, 1, 1);
        grid.add(rotuloHorario, 0, 2);
        grid.add(campoHorario, 1, 2);
        grid.add(rotuloData, 0, 3);
        grid.add(campoData, 1, 3);

        Button btnSalvar = new Button("Salvar");
        Button btnCancelar = new Button("Cancelar");

        btnSalvar.setOnAction(e -> {
            if (campoModelo.getValue() == null || campoProduto.getValue() == null ||
                    campoData.getValue() == null || !horarioValido(campoHorario.getText())) {
                mostrarAviso("Preencha os campos e informe o horário no formato HH:mm.");
                return;
            }

            agendamento.setModelo(campoModelo.getValue());
            agendamento.setProduto(campoProduto.getValue());
            agendamento.setData_hora_agendamento(campoData.getValue().toString()); // ISO internamente
            agendamento.setHorario(campoHorario.getText());
            dao.editarAgendamento(agendamento);
            popup.close();
            atualizarTabela();
        });
        btnCancelar.setOnAction(e -> popup.close());

        HBox botoes = new HBox(10, btnSalvar, btnCancelar);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setPadding(new Insets(0, 20, 15, 0));

        VBox conteudo = new VBox(10, grid, botoes);
        conteudo.getStyleClass().add("popup-edicao-agendamento");
        Scene cena = new Scene(conteudo, 440, 330);
        cena.getStylesheets().add(MainApplication.class
                .getResource("/org/githubio/desktop_beleza/css/style.css").toExternalForm());
        popup.setScene(cena);
        popup.setResizable(false);
        popup.showAndWait();
    }

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void atualizarVisaoGeral() {
        String usuario = MainApplication.getUsuario();
        lblPraticasDoMes.setText(String.valueOf(dao.contarPraticasDoMes(usuario)));
        lblVagasOcupadas.setText(dao.calcularPercentualVagasOcupadas(usuario) + "%");
        lblTurmasAtivas.setText(String.valueOf(dao.contarTurmasAtivas(usuario)));
    }
}
