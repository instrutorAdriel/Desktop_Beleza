package org.githubio.desktop_beleza.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.dao.ServicosDAO;
import org.githubio.desktop_beleza.model.Unidade;
import org.githubio.desktop_beleza.model.dao.UnidadeDAO;

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

        tabelaUnidades.widthProperty().addListener((observable, larguraAnterior, novaLargura) ->
                ajustarLarguraColunas(novaLargura.doubleValue())
        );
        ajustarLarguraColunas(tabelaUnidades.getWidth());

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

    private void ajustarLarguraColunas(double larguraTabela) {
        double larguraUtil = larguraTabela - tabelaUnidades.getInsets().getLeft()
                - tabelaUnidades.getInsets().getRight() - 2;
        if (larguraUtil <= 0) {
            return;
        }

        colNome.setPrefWidth(larguraUtil * 0.26);
        colEndereco.setPrefWidth(larguraUtil * 0.55);
        colSituacao.setPrefWidth(larguraUtil * 0.19);
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
    private void abrirVinculoServico() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    MainApplication.class.getResource(
                            "VinculoServicoUnidade.fxml"
                    )
            );

            Parent root = loader.load();

            VinculoServicoUnidadeController controller =
                    loader.getController();

            controller.setUnidades(unidadeDAO.listarTodas());

            controller.setServicos(new ServicosDAO().lerTodos());

            // Já deixa selecionada a unidade clicada na tabela
            controller.selecionarUnidade(
                    tabelaUnidades.getSelectionModel().getSelectedItem()
            );

            Stage stage = new Stage();

            stage.initOwner(tabelaUnidades.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Adicionar serviço à unidade");
            stage.setResizable(false);
            stage.setScene(new Scene(root));

            stage.showAndWait();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarMensagem(
                    Alert.AlertType.ERROR,
                    "Não foi possível abrir a tela de vínculo."
            );
        }
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