package org.githubio.desktop_beleza.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.githubio.desktop_beleza.model.Agendamento;
import org.githubio.desktop_beleza.model.dao.AgendaDAO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class HistoricoController {

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

    private final AgendaDAO dao = new AgendaDAO();

    @FXML
    public void initialize() {
        txtServico.getItems().add(TODOS_SERVICOS);
        txtServico.getItems().addAll(dao.listarProdutos());
        txtServico.getSelectionModel().selectFirst();

        cbUnidade.getItems().add(TODAS_UNIDADES);
        cbUnidade.getItems().addAll(dao.listarUnidades());
        cbUnidade.getSelectionModel().selectFirst();

        dpData.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : FMT_BR.format(data);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.isBlank()) return null;
                try {
                    return LocalDate.parse(texto.trim(), FMT_BR);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
        dpData.setPromptText("dd/mm/aaaa");

        colData.setCellValueFactory(new PropertyValueFactory<>("dataHoraAgendamentoFormatada"));
        colServico.setCellValueFactory(new PropertyValueFactory<>("servico"));
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tabelaAgenda.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        atualizarTabela();
    }

    @FXML
    protected void onBuscarClick() {
        atualizarTabela();
    }

    @FXML
    protected void onLimparFiltroClick() {
        dpData.setValue(null);
        dpData.getEditor().clear();
        txtServico.getSelectionModel().selectFirst();
        cbUnidade.getSelectionModel().selectFirst();
        atualizarTabela();
    }

    private void atualizarTabela() {
        String unidade = TODAS_UNIDADES.equals(cbUnidade.getValue()) ? null : cbUnidade.getValue();
        String servico = TODOS_SERVICOS.equals(txtServico.getValue()) ? null : txtServico.getValue();
        tabelaAgenda.setItems(FXCollections.observableArrayList(
                dao.buscarHistorico(dpData.getValue(), unidade, servico)));
    }

    private String paraDataBr(String iso) {
        try {
            return LocalDate.parse(iso).format(FMT_BR);
        } catch (Exception e) {
            return iso;
        }
    }
}
