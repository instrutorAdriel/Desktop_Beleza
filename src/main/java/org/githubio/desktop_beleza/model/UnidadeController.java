package org.githubio.desktop_beleza.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.Unidade;
import org.githubio.desktop_beleza.model.UnidadeDAO;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UnidadeController implements Initializable {

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtEndereco;

    @FXML
    private ComboBox<String> cbSituacao;

    @FXML
    private TableView<Unidade> tabelaUnidades;

    @FXML
    private TableColumn<Unidade, String> colNome;

    @FXML
    private TableColumn<Unidade, String> colEndereco;

    @FXML
    private TableColumn<Unidade, String> colSituacao;

    private final UnidadeDAO unidadeDAO = new UnidadeDAO();

    private Unidade unidadeSelecionada;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        cbSituacao.getItems().addAll(
                "Ativa",
                "Desativada"
        );

        cbSituacao.setValue("Ativa");

        colNome.setCellValueFactory(
                new PropertyValueFactory<>("nome")
        );

        colEndereco.setCellValueFactory(
                new PropertyValueFactory<>("endereco")
        );

        colSituacao.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        carregarTabela();


        tabelaUnidades.setOnMouseClicked(event -> {

            Unidade unidade =
                    tabelaUnidades.getSelectionModel().getSelectedItem();

            if (unidade != null) {

                unidadeSelecionada = unidade;

                txtNome.setText(unidade.getNome());

                txtEndereco.setText(
                        unidade.getEndereco()
                );

                if ("A".equals(unidade.getSituacao())) {
                    cbSituacao.setValue("Ativa");
                } else {
                    cbSituacao.setValue("Desativada");
                }
            }
        });
    }


    private void carregarTabela() {

        tabelaUnidades.setItems(
                FXCollections.observableArrayList(
                        unidadeDAO.listarTodas()
                )
        );
    }


    @FXML
    private void salvar() {

        String nome = txtNome.getText().trim();
        String endereco = txtEndereco.getText().trim();

        if (nome.isEmpty()) {

            mostrarMensagem(
                    Alert.AlertType.WARNING,
                    "Informe o nome da unidade."
            );

            return;
        }


        String situacao =
                "Ativa".equals(cbSituacao.getValue())
                        ? "A"
                        : "D";


        if (unidadeSelecionada == null) {

            Unidade novaUnidade =
                    new Unidade(
                            nome,
                            endereco,
                            situacao
                    );

            unidadeDAO.inserir(novaUnidade);

            mostrarMensagem(
                    Alert.AlertType.INFORMATION,
                    "Unidade cadastrada com sucesso!"
            );

        } else {

            unidadeSelecionada.setNome(nome);
            unidadeSelecionada.setEndereco(endereco);
            unidadeSelecionada.setSituacao(situacao);

            unidadeDAO.atualizar(unidadeSelecionada);

            mostrarMensagem(
                    Alert.AlertType.INFORMATION,
                    "Unidade atualizada com sucesso!"
            );
        }

        limparCampos();

        carregarTabela();
    }


    @FXML
    private void novoCadastro() {

        limparCampos();
    }


    private void limparCampos() {

        txtNome.clear();

        txtEndereco.clear();

        cbSituacao.setValue("Ativa");

        unidadeSelecionada = null;

        tabelaUnidades
                .getSelectionModel()
                .clearSelection();
    }


    private void mostrarMensagem(
            Alert.AlertType tipo,
            String mensagem
    ) {

        Alert alert = new Alert(tipo);

        alert.setHeaderText(null);

        alert.setContentText(mensagem);

        alert.showAndWait();
    }


    @FXML
    public void trocarTelaParaPaginaInicial()
            throws IOException {

        MainApplication.setRoot("Telaagenda");
    }


    @FXML
    public void trocarTelaParaModelos()
            throws IOException {

        MainApplication.setRoot(
                "gerenciarmodelo"
        );
    }


    @FXML
    public void trocarTelaParaServicos()
            throws IOException {

        MainApplication.setRoot(
                "servicos"
        );
    }


    @FXML
    public void trocarTelaParaTurmas()
            throws IOException {

        MainApplication.setRoot(
                "GerenciarTurma"
        );
    }


    @FXML
    public void sairDoSistema()
            throws IOException {

        MainApplication.setUsuario("");

        MainApplication.setRoot("login");
    }
}