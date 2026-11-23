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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
import org.githubio.desktop_beleza.model.Unidade;
import org.githubio.desktop_beleza.model.dao.DisponibilidadeDAO;
import org.githubio.desktop_beleza.model.dao.ServicosDAO;
import org.githubio.desktop_beleza.model.dao.UnidadeDAO;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ServicosController implements Initializable {

    @FXML private TableView<Servico> tabelaServicos;
    @FXML private TableColumn<Servico, String> colNome;
    @FXML private TableColumn<Servico, String> colDescricao;
    @FXML private TableColumn<Servico, Void> colAcoes;
    @FXML private TextField txtBuscar;
    @FXML private Button btnDisponibilizar;

    private final ServicosDAO dao = new ServicosDAO();
    private final UnidadeDAO unidadeDAO = new UnidadeDAO();
    private final DisponibilidadeDAO disponibilidadeDAO = new DisponibilidadeDAO();

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

        // O botão "Disponibilizar agendamento" só vale para um serviço selecionado
        btnDisponibilizar.disableProperty().bind(
                tabelaServicos.getSelectionModel().selectedItemProperty().isNull());

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

    @FXML
    private void abrirDialogoDisponibilizar(ActionEvent event) {
        Servico servico = tabelaServicos.getSelectionModel().getSelectedItem();
        if (servico == null) {
            mostrarAviso("Selecione um serviço na tabela para disponibilizar na agenda.");
            return;
        }

        java.util.List<Unidade> unidadesAtivas;
        try {
            unidadesAtivas = unidadeDAO.listarTodas().stream()
                    .filter(u -> "A".equalsIgnoreCase(u.getSituacao()))
                    .toList();
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível carregar as unidades", e);
            return;
        }
        if (unidadesAtivas.isEmpty()) {
            mostrarAviso("Não há unidades ativas cadastradas. Cadastre ou ative uma unidade primeiro.");
            return;
        }

        java.util.List<DisponibilidadeDAO.TurmaOpcao> turmasAtivas;
        try {
            turmasAtivas = disponibilidadeDAO.listarTurmasAtivas();
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível carregar as turmas", e);
            return;
        }
        if (turmasAtivas.isEmpty()) {
            mostrarAviso("Não há turmas em andamento cadastradas. Cadastre uma turma primeiro.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Disponibilizar na Agenda");
        dialog.setHeaderText("Disponibilizar \"" + servico.getNome() + "\" na agenda");

        ButtonType btnConfirmar = new ButtonType("Disponibilizar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, ButtonType.CANCEL);

        ComboBox<Unidade> cbUnidade = new ComboBox<>(FXCollections.observableArrayList(unidadesAtivas));
        cbUnidade.setPromptText("Selecione a unidade");
        cbUnidade.setMaxWidth(Double.MAX_VALUE);
        javafx.util.StringConverter<Unidade> conversorUnidade = new javafx.util.StringConverter<>() {
            @Override public String toString(Unidade u) { return u == null ? "" : u.getNome(); }
            @Override public Unidade fromString(String texto) { return null; }
        };
        cbUnidade.setConverter(conversorUnidade);

        ComboBox<DisponibilidadeDAO.TurmaOpcao> cbTurma = new ComboBox<>(FXCollections.observableArrayList(turmasAtivas));
        cbTurma.setPromptText("Selecione a turma");
        cbTurma.setMaxWidth(Double.MAX_VALUE);

        DatePicker dpData = new DatePicker();
        dpData.setPromptText("dd/mm/aaaa");
        dpData.setMaxWidth(Double.MAX_VALUE);
        // Não permite datas passadas
        dpData.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate dia, boolean empty) {
                super.updateItem(dia, empty);
                setDisable(empty || dia.isBefore(LocalDate.now()));
            }
        });

        // Horários de 30 em 30 minutos, das 06:00 às 22:00
        ObservableList<LocalTime> horarios = FXCollections.observableArrayList();
        for (LocalTime h = LocalTime.of(6, 0); !h.isAfter(LocalTime.of(22, 0)); h = h.plusMinutes(30)) {
            horarios.add(h);
        }
        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("HH:mm");
        javafx.util.StringConverter<LocalTime> conversorHora = new javafx.util.StringConverter<>() {
            @Override public String toString(LocalTime h) { return h == null ? "" : h.format(fmtHora); }
            @Override public LocalTime fromString(String texto) { return null; }
        };
        ComboBox<LocalTime> cbInicio = new ComboBox<>(horarios);
        cbInicio.setConverter(conversorHora);
        cbInicio.setPromptText("Início");
        cbInicio.setMaxWidth(Double.MAX_VALUE);
        ComboBox<LocalTime> cbFim = new ComboBox<>(horarios);
        cbFim.setConverter(conversorHora);
        cbFim.setPromptText("Término");
        cbFim.setMaxWidth(Double.MAX_VALUE);

        // Ao escolher o início, sugere o término uma hora depois
        cbInicio.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo != null && (cbFim.getValue() == null || !cbFim.getValue().isAfter(novo))) {
                LocalTime sugestao = novo.plusHours(1);
                cbFim.setValue(horarios.contains(sugestao) ? sugestao : null);
            }
        });

        Spinner<Integer> spVagas = new Spinner<>();
        spVagas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        spVagas.setEditable(true);
        spVagas.setMaxWidth(Double.MAX_VALUE);

        HBox linhaHorario = new HBox(10, cbInicio, cbFim);
        HBox.setHgrow(cbInicio, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(cbFim, javafx.scene.layout.Priority.ALWAYS);

        Label lblErro = new Label();
        lblErro.setStyle("-fx-text-fill: #c0392b;");
        lblErro.setWrapText(true);

        VBox conteudo = new VBox(8,
                new Label("Unidade:"), cbUnidade,
                new Label("Turma:"), cbTurma,
                new Label("Data:"), dpData,
                new Label("Horário (início e término):"), linhaHorario,
                new Label("Quantidade de vagas:"), spVagas,
                lblErro);
        conteudo.setPrefWidth(360);
        dialog.getDialogPane().setContent(conteudo);

        // Valida sem fechar a janela: mostra o erro e mantém o diálogo aberto
        Button botaoOk = (Button) dialog.getDialogPane().lookupButton(btnConfirmar);
        botaoOk.addEventFilter(ActionEvent.ACTION, ev -> {
            String erro = validarDisponibilidade(cbUnidade.getValue(), cbTurma.getValue(), dpData.getValue(),
                    cbInicio.getValue(), cbFim.getValue(), spVagas);
            if (erro != null) {
                lblErro.setText(erro);
                ev.consume();
            }
        });

        dialog.showAndWait().ifPresent(resposta -> {
            if (resposta != btnConfirmar) return;
            try {
                boolean criada = disponibilidadeDAO.disponibilizar(
                        servico.getId(), cbUnidade.getValue().getId(), cbTurma.getValue().id(), dpData.getValue(),
                        cbInicio.getValue(), cbFim.getValue(), spVagas.getValue());

                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText(criada ? "Serviço disponibilizado na agenda!"
                        : "Vagas adicionadas à disponibilidade existente!");
                ok.setContentText(servico.getNome() + "\n"
                        + "Unidade: " + cbUnidade.getValue().getNome() + "\n"
                        + "Turma: " + cbTurma.getValue().nome() + "\n"
                        + "Data: " + dpData.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n"
                        + "Horário: " + cbInicio.getValue().format(fmtHora) + " às " + cbFim.getValue().format(fmtHora) + "\n"
                        + "Vagas: " + spVagas.getValue());
                ok.showAndWait();
            } catch (RuntimeException e) {
                mostrarErro("Não foi possível disponibilizar o serviço", e);
            }
        });
    }

    private String validarDisponibilidade(Unidade unidade, DisponibilidadeDAO.TurmaOpcao turma, LocalDate data, LocalTime inicio,
                                          LocalTime fim, Spinner<Integer> spVagas) {
        // Garante que o valor digitado no spinner foi confirmado
        try {
            spVagas.commitValue();
        } catch (RuntimeException e) {
            return "Informe uma quantidade de vagas válida.";
        }
        if (unidade == null) return "Selecione a unidade.";
        if (turma == null) return "Selecione a turma.";
        if (data == null) return "Selecione a data.";
        if (data.isBefore(LocalDate.now())) return "A data não pode estar no passado.";
        if (inicio == null || fim == null) return "Selecione o horário de início e de término.";
        if (!fim.isAfter(inicio)) return "O horário de término deve ser depois do início.";
        if (data.equals(LocalDate.now()) && !inicio.isAfter(LocalTime.now())) {
            return "O horário de início já passou para hoje.";
        }
        Integer vagas = spVagas.getValue();
        if (vagas == null || vagas < 1) return "A quantidade de vagas deve ser de pelo menos 1.";
        return null;
    }

    private void mostrarAviso(String mensagem) {
        new Alert(Alert.AlertType.WARNING, mensagem, ButtonType.OK).showAndWait();
    }

    private void mostrarErro(String titulo, RuntimeException e) {
        Alert erro = new Alert(Alert.AlertType.ERROR);
        erro.setHeaderText(titulo);
        erro.setContentText(e.getMessage());
        erro.showAndWait();
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