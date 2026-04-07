package org.githubio.desktop_beleza;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class AgendaController {
    @FXML private ComboBox<String> txtServico;
    @FXML private DatePicker dpData;
    @FXML private TextField txtPratica;
    @FXML private TextField txtCliente;
    @FXML private TableView<Agenda> tabelaAgenda;
    @FXML private TableColumn<Agenda, String> colData;
    @FXML private TableColumn<Agenda, String> colServico;
    @FXML private TableColumn<Agenda, String> colPratica;
    @FXML private TableColumn<Agenda, String> colCliente;
    @FXML private TableColumn<Agenda, String> colStatus;
    @FXML private TableColumn<Agenda, String> colAcao;

    private Agenda agendaSendoEditada = null;

    @FXML
    public void initialize() {
        // 1. Configuração do ComboBox
        txtServico.getItems().addAll("Cabeleireiro", "Barbeiro", "Maquiador", "Design de Sobrancelhas");

        // 2. Vincula os dados (Model) às colunas
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colServico.setCellValueFactory(new PropertyValueFactory<>("servico"));
        colPratica.setCellValueFactory(new PropertyValueFactory<>("pratica"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 3. Trava a ordem das colunas
        tabelaAgenda.getColumns().addListener(new ListChangeListener<TableColumn<Agenda, ?>>() {
            private boolean suspender = false;
            @Override
            public void onChanged(Change<? extends TableColumn<Agenda, ?>> c) {
                while (c.next()) {
                    if (!suspender && (c.wasReplaced() || c.wasAdded() || c.wasRemoved())) {
                        suspender = true;
                        tabelaAgenda.getColumns().setAll(colData, colServico, colPratica, colCliente, colStatus, colAcao);
                        suspender = false;
                    }
                }
            }
        });

        tabelaAgenda.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);


        // 4. Configuração da Coluna de Ação (Editar/Excluir)
        colAcao.setCellFactory(parm -> new TableCell<>() {
            private final MenuButton mnuOpcoes = new MenuButton("...");
            private final MenuItem itemEdit = new MenuItem("Editar");
            private final MenuItem itemDel = new MenuItem("Excluir");

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

                    // AÇÃO EXCLUIR
                    itemDel.setOnAction(e -> {
                        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
                        alerta.setTitle("Excluir Agendamento");
                        alerta.setHeaderText("Deseja excluir o cliente " + agendaDaLinha.getCliente() + "?");
                        if (alerta.showAndWait().get() == ButtonType.OK) {
                            new AgendaDAO().excluirAgendamento(agendaDaLinha.getId());
                            atualizarTabela();
                        }
                    });

                    // AÇÃO EDITAR
                    itemEdit.setOnAction(e -> {
                        txtCliente.setText(agendaDaLinha.getCliente());
                        txtServico.setValue(agendaDaLinha.getServico());
                        txtPratica.setText(agendaDaLinha.getPratica());

                        agendaSendoEditada = agendaDaLinha;
                        IO.println("Editando ID: " + agendaSendoEditada.getId());
                    });

                    setGraphic(mnuOpcoes);
                }
            }
        });

        // Carrega os dados assim que abre a tela
        atualizarTabela();
    }
    @FXML
    protected void onSalvarButtonClick() {
        if (txtServico.getValue() == null || dpData.getValue() == null ||
                txtPratica.getText().isBlank() || txtCliente.getText().isBlank()) {
            IO.println("Erro: Preencha todos os campos!");
            return;
        }

        String data = String.valueOf(dpData.getValue());
        String servico = txtServico.getValue();
        String pratica = txtPratica.getText();
        String cliente = txtCliente.getText();

        AgendaDAO dao = new AgendaDAO();

        if (agendaSendoEditada != null) {
            // MODO EDIÇÃO
            agendaSendoEditada.setData(data);
            agendaSendoEditada.setServico(servico);
            agendaSendoEditada.setPratica(pratica);
            agendaSendoEditada.setCliente(cliente);

            dao.editarAgendamento(agendaSendoEditada);
            agendaSendoEditada = null; // Limpa para o próximo ser novo cadastro
        } else {
            // MODO NOVO CADASTRO
            dao.cadastrarAgendamento(data, servico, pratica, cliente);
        }

        limparCampos();
        atualizarTabela();
    }

    private void limparCampos() {
        txtCliente.clear();
        txtPratica.clear();
        dpData.setValue(null);
        txtServico.setValue(null);
    }

    public void atualizarTabela() {
        List<Agenda> listaRecarregada = new AgendaDAO().listarAgendamentos();
        tabelaAgenda.setItems(FXCollections.observableArrayList(listaRecarregada));
    }
}
