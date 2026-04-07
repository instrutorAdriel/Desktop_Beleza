package org.example.demo;


import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.demo.dao.UsuarioDAO;

import java.util.List;

public class HelloController {

    @FXML private TableView<Servico> tabelaServicos;
    @FXML private TableColumn<Servico, String> colNome;
    @FXML private TableColumn<Servico, String> colDescricao;
    @FXML private TableColumn<Servico, String> colHorario;

    @FXML private TextField txtNome;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtHorario;
    @FXML private TextField txtBuscar;

    private ObservableList<Servico> listaServicos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNome()));
        colDescricao.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescricao()));
        colHorario.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getHorario()));

        listaServicos.addAll(
                new Servico("Cabeleireiro", "Corte e estilo", "09:00 - 18:00"),
                new Servico("Barbeiro", "Barba e cabelo", "10:00 - 19:00"),
                new Servico("Maquiador", "Maquiagem profissional", "08:00 - 17:00"),
                new Servico("Design de sobrancelhas", "Modelagem", "09:00 - 16:00")
        );

        tabelaServicos.setItems(listaServicos);

        tabelaServicos.setOnMouseClicked(e -> selecionar());
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
    private void salvar() {
        listaServicos.add(new Servico(
                txtNome.getText(),
                txtDescricao.getText(),
                txtHorario.getText()
        ));
    }

    @FXML
    private void editar() {
        Servico s = tabelaServicos.getSelectionModel().getSelectedItem();
        if (s != null) {
            s.setNome(txtNome.getText());
            s.setDescricao(txtDescricao.getText());
            s.setHorario(txtHorario.getText());
            tabelaServicos.refresh();
        }
    }

    @FXML
    private void buscar() {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        List<String> lista= usuarioDAO.lerTodos();
        String texto = txtBuscar.getText().toLowerCase();

        if (texto.isEmpty()) {
            tabelaServicos.setItems(listaServicos);
            return;
        }

        ObservableList<Servico> filtrada = FXCollections.observableArrayList();

        for (Servico s : listaServicos) {
            if (s.getNome().toLowerCase().contains(texto)) {
                filtrada.add(s);
            }
        }

        tabelaServicos.setItems(filtrada);
    }

}
