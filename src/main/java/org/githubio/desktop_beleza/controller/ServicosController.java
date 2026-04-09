package org.githubio.desktop_beleza.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleStringProperty;

import org.githubio.desktop_beleza.model.Servico;
import org.githubio.desktop_beleza.model.ServicosDAO;

public class ServicosController {

    @FXML private TableView<Servico> tabelaServicos;
    @FXML private TableColumn<Servico, String> colNome;
    @FXML private TableColumn<Servico, String> colDescricao;
    @FXML private TableColumn<Servico, String> colHorario;

    @FXML private TextField txtNome;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtHorario;
    @FXML private TextField txtBuscar;

    private ObservableList<Servico> listaServicos = FXCollections.observableArrayList();
    private ServicosDAO servicosDAO = new ServicosDAO();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNome()));
        colDescricao.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDescricao()));
        colHorario.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getHorario()));

        carregarDados();
        tabelaServicos.setOnMouseClicked(e -> selecionar());
    }

    private void carregarDados() {
        listaServicos.clear();
        listaServicos.addAll(servicosDAO.lerTodos());
        tabelaServicos.setItems(listaServicos);
    }

    private void selecionar() {
        Servico s = tabelaServicos.getSelectionModel().getSelectedItem();
        if (s != null) {
            txtNome.setText(s.getNome());
            txtDescricao.setText(s.getDescricao());
            txtHorario.setText(s.getHorario());
        }
    }


    @FXML
    private void editar() {
        if (tabelaServicos.getSelectionModel().getSelectedItem() == null) {
            alerta("Selecione um serviço para editar");
        }
    }


    @FXML
    private void salvar() {
        Servico s = tabelaServicos.getSelectionModel().getSelectedItem();

        if (s == null) {
            alerta("Selecione um serviço para salvar");
            return;
        }

        if (txtNome.getText().isEmpty() ||
                txtDescricao.getText().isEmpty() ||
                txtHorario.getText().isEmpty()) {
            alerta("Preencha todos os campos");
            return;
        }

        s.setNome(txtNome.getText());
        s.setDescricao(txtDescricao.getText());
        s.setHorario(txtHorario.getText());

        servicosDAO.atualizar(s);
        tabelaServicos.refresh();

        alertaSucesso("Serviço atualizado com sucesso!");
    }


    @FXML
    private void buscar() {
        String filtro = txtBuscar.getText().toLowerCase();

        if (filtro.isEmpty()) {
            carregarDados();
            return;
        }

        ObservableList<Servico> filtrada = FXCollections.observableArrayList();
        for (Servico s : listaServicos) {
            if (s.getNome().toLowerCase().contains(filtro)) {
                filtrada.add(s);
            }
        }
        tabelaServicos.setItems(filtrada);
    }


    private void alerta(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).show();
    }

    private void alertaSucesso(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}
