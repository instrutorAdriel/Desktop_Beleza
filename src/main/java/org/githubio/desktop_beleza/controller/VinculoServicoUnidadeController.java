package org.githubio.desktop_beleza.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.githubio.desktop_beleza.model.Produto;
import org.githubio.desktop_beleza.model.Unidade;
import org.githubio.desktop_beleza.model.dao.UnidadeDAO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VinculoServicoUnidadeController {

    @FXML
    private ComboBox<Unidade> cbUnidade;

    @FXML
    private ComboBox<Produto> cbServico;

    @FXML
    private ListView<Produto> listaServicosVinculados;

    private final UnidadeDAO unidadeDAO = new UnidadeDAO();
    private List<Produto> todosProdutos = List.of();


    @FXML
    public void initialize() {

        // Mostra só o nome dentro do select
        cbUnidade.setConverter(new StringConverter<>() {
            @Override
            public String toString(Unidade unidade) {
                return unidade == null ? "" : unidade.getNome();
            }

            @Override
            public Unidade fromString(String texto) {
                return null;
            }
        });

        cbServico.setConverter(new StringConverter<>() {
            @Override
            public String toString(Produto produto) {
                return produto == null ? "" : produto.getNome();
            }

            @Override
            public Produto fromString(String texto) {
                return null;
            }
        });

        listaServicosVinculados.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Produto produto, boolean vazio) {
                super.updateItem(produto, vazio);
                setText(vazio || produto == null ? null : produto.getNome());
            }
        });

        cbUnidade.valueProperty().addListener((observable, anterior, atual) ->
                atualizarServicosDaUnidade()
        );
    }


    // Chamados pela tela de Unidades antes de abrir a janela
    public void setUnidades(List<Unidade> unidades) {
        cbUnidade.setItems(FXCollections.observableArrayList(unidades));
    }

    public void setServicos(List<Produto> produtos) {
        todosProdutos = List.copyOf(produtos);
        atualizarServicosDaUnidade();
    }

    public void selecionarUnidade(Unidade unidade) {

        if (unidade == null) {
            return;
        }

        // Procura pelo id, pois a lista do select é outra instância
        for (Unidade item : cbUnidade.getItems()) {
            if (item.getId() == unidade.getId()) {
                cbUnidade.getSelectionModel().select(item);
                return;
            }
        }
    }


    @FXML
    private void vincular() {

        Unidade unidade = cbUnidade.getValue();
        Produto produto = cbServico.getValue();

        if (unidade == null || produto == null) {

            mostrarMensagem(
                    Alert.AlertType.WARNING,
                    "Selecione a unidade e o procedimento."
            );

            return;
        }

        try {

            boolean criado = unidadeDAO.vincularServico(
                    unidade.getId(),
                    produto.getId()
            );

            if (!criado) {

                mostrarMensagem(
                        Alert.AlertType.INFORMATION,
                        "O procedimento \"" + produto.getNome()
                                + "\" já está vinculado à unidade \""
                                + unidade.getNome() + "\"."
                );

                return;
            }

            mostrarMensagem(
                    Alert.AlertType.INFORMATION,
                    "Procedimento vinculado à unidade com sucesso!"
            );

            cbServico.getSelectionModel().clearSelection();
            atualizarServicosDaUnidade();

        } catch (RuntimeException e) {

            e.printStackTrace();

            String detalhe = e.getCause() != null
                    ? e.getCause().getMessage()
                    : e.getMessage();

            mostrarMensagem(
                    Alert.AlertType.ERROR,
                    "Não foi possível criar o vínculo.\n\n" + detalhe
            );
        }
    }

    @FXML
    private void desvincular() {
        Unidade unidade = cbUnidade.getValue();
        Produto produto = listaServicosVinculados.getSelectionModel().getSelectedItem();

        if (unidade == null || produto == null) {
            mostrarMensagem(Alert.AlertType.WARNING, "Selecione um procedimento vinculado para remover.");
            return;
        }

        Alert confirmacao = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Desvincular \"" + produto.getNome() + "\" de \"" + unidade.getNome() + "\"?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmacao.setHeaderText(null);
        if (confirmacao.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        try {
            unidadeDAO.desvincularServico(unidade.getId(), produto.getId());
            atualizarServicosDaUnidade();
            mostrarMensagem(Alert.AlertType.INFORMATION, "Procedimento desvinculado da unidade.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            mostrarMensagem(
                    Alert.AlertType.ERROR,
                    e.getMessage() == null
                            ? "Não foi possível desvincular o procedimento."
                            : e.getMessage()
            );
        }
    }

    private void atualizarServicosDaUnidade() {
        Unidade unidade = cbUnidade.getValue();
        if (unidade == null) {
            listaServicosVinculados.getItems().clear();
            cbServico.getItems().clear();
            return;
        }

        try {
            List<Produto> vinculados = unidadeDAO.listarServicosVinculados(unidade.getId());
            listaServicosVinculados.setItems(FXCollections.observableArrayList(vinculados));

            Set<Integer> idsVinculados = vinculados.stream()
                    .map(Produto::getId)
                    .collect(Collectors.toSet());
            cbServico.setItems(FXCollections.observableArrayList(
                    todosProdutos.stream()
                            .filter(produto -> !idsVinculados.contains(produto.getId()))
                            .toList()
            ));
        } catch (RuntimeException e) {
            e.printStackTrace();
            listaServicosVinculados.getItems().clear();
            cbServico.getItems().clear();
            mostrarMensagem(
                    Alert.AlertType.ERROR,
                    e.getMessage() == null
                            ? "Não foi possível carregar os procedimentos desta unidade."
                            : e.getMessage()
            );
        }
    }


    @FXML
    private void cancelar() {
        fechar();
    }


    private void fechar() {
        ((Stage) cbUnidade.getScene().getWindow()).close();
    }


    private void mostrarMensagem(Alert.AlertType tipo, String mensagem) {

        Alert alert = new Alert(tipo);

        alert.setHeaderText(null);

        alert.setContentText(mensagem);

        alert.showAndWait();
    }
}
