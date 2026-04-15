package org.githubio.desktop_beleza.controller;

import org.githubio.desktop_beleza.model.Agenda;
import org.githubio.desktop_beleza.model.AgendaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class AgendaController {
    @FXML private ComboBox<String> txtServico;
    @FXML private DatePicker dpData;
    @FXML private TextField txtPratica;
    @FXML private TextField txtCliente;
    @FXML private TextField txtHorario;
    @FXML private TableView<Agenda> tabelaAgenda;
    @FXML private TableColumn<Agenda, String> colData;
    @FXML private TableColumn<Agenda, String> colServico;
    @FXML private TableColumn<Agenda, String> colPratica;
    @FXML private TableColumn<Agenda, String> colCliente;
    @FXML private TableColumn<Agenda, String> colHorario;
    @FXML private TableColumn<Agenda, String> colStatus;
    @FXML private TableColumn<Agenda, String> colAcao;

    private Agenda agendaSendoEditada = null;

    @FXML
    public void initialize() {
        // 1. Configuração do ComboBox
        txtServico.getItems().addAll("Cabeleireiro", "Barbeiro", "Maquiador", "Design de Sobrancelhas");

        // 2. Vincula os dados (org.githubio.desktop_beleza.Model) às colunas
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colServico.setCellValueFactory(new PropertyValueFactory<>("servico"));
        colPratica.setCellValueFactory(new PropertyValueFactory<>("pratica"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 3. Trava a ordem das colunas
        tabelaAgenda.getColumns().addListener(new ListChangeListener<TableColumn<Agenda, ?>>() {
            private boolean suspender = false;
            @Override
            public void onChanged(Change<? extends TableColumn<Agenda, ?>> c) {
                while (c.next()) {
                    if (!suspender && (c.wasReplaced() || c.wasAdded() || c.wasRemoved())) {
                        suspender = true;
                        tabelaAgenda.getColumns().setAll(colData, colServico, colPratica, colCliente,colHorario, colStatus, colAcao);
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
                        txtHorario.setText(agendaDaLinha.getHorario());

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
                txtPratica.getText().isBlank() || txtCliente.getText().isBlank() ||
                txtHorario.getText().isBlank()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Preencha todos os campos!");
            alert.show();
            return;
        }

        // Coleta de dados
        String data = String.valueOf(dpData.getValue());
        String servico = txtServico.getValue();
        String pratica = txtPratica.getText();
        String cliente = txtCliente.getText();
        String horario = txtHorario.getText();

        // ✅ STATUS CORRETO PARA O ENUM DO MYSQL
        String status = "Compareceu";   // <<< AQUI RESOLVE
        String turma = "1";

        AgendaDAO dao = new AgendaDAO();

        if (agendaSendoEditada != null) {

            agendaSendoEditada.setData(data);
            agendaSendoEditada.setServico(servico);
            agendaSendoEditada.setPratica(pratica);
            agendaSendoEditada.setCliente(cliente);
            agendaSendoEditada.setHorario(horario);

            dao.editarAgendamento(agendaSendoEditada);
            agendaSendoEditada = null;

        } else {

            dao.cadastrarAgendamento(
                    pratica,
                    data,
                    horario,
                    status,      // ✅ agora bate com o ENUM
                    cliente,
                    servico,
                    turma
            );
        }

        limparCampos();
        atualizarTabela();
    }

    private void limparCampos() {
        txtCliente.clear();
        txtPratica.clear();
        txtHorario.clear(); // Não esqueça de limpar o horário
        dpData.setValue(null);
        txtServico.setValue(null);
    }

    public void atualizarTabela() {
        List<Agenda> listaRecarregada = new AgendaDAO().listarAgendamentos();
        tabelaAgenda.setItems(FXCollections.observableArrayList(listaRecarregada));

    }
}
