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
import org.githubio.desktop_beleza.model.Servico;
import org.githubio.desktop_beleza.model.Unidade;
import org.githubio.desktop_beleza.model.dao.UnidadeDAO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VinculoServicoUnidadeController {

    @FXML
    private ComboBox<Unidade> cbUnidade;

    @FXML
    private ComboBox<Servico> cbServico;

    @FXML
    private ListView<Servico> listaServicosVinculados;

    private final UnidadeDAO unidadeDAO = new UnidadeDAO();
    private List<Servico> todosServicos = List.of();


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
            public String toString(Servico servico) {
                return servico == null ? "" : servico.getNome();
            }

            @Override
            public Servico fromString(String texto) {
                return null;
            }
        });

        listaServicosVinculados.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Servico servico, boolean vazio) {
                super.updateItem(servico, vazio);
                setText(vazio || servico == null ? null : servico.getNome());
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

    public void setServicos(List<Servico> servicos) {
        todosServicos = List.copyOf(servicos);
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
        Servico servico = cbServico.getValue();

        if (unidade == null || servico == null) {

            mostrarMensagem(
                    Alert.AlertType.WARNING,
                    "Selecione a unidade e o serviço."
            );

            return;
        }

        try {

            boolean criado = unidadeDAO.vincularServico(
                    unidade.getId(),
                    servico.getId()
            );

            if (!criado) {

                mostrarMensagem(
                        Alert.AlertType.INFORMATION,
                        "O serviço \"" + servico.getNome()
                                + "\" já está vinculado à unidade \""
                                + unidade.getNome() + "\"."
                );

                return;
            }

            mostrarMensagem(
                    Alert.AlertType.INFORMATION,
                    "Serviço vinculado à unidade com sucesso!"
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
        Servico servico = listaServicosVinculados.getSelectionModel().getSelectedItem();

        if (unidade == null || servico == null) {
            mostrarMensagem(Alert.AlertType.WARNING, "Selecione um serviço vinculado para remover.");
            return;
        }

        Alert confirmacao = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Desvincular \"" + servico.getNome() + "\" de \"" + unidade.getNome() + "\"?",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmacao.setHeaderText(null);
        if (confirmacao.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        try {
            unidadeDAO.desvincularServico(unidade.getId(), servico.getId());
            atualizarServicosDaUnidade();
            mostrarMensagem(Alert.AlertType.INFORMATION, "Serviço desvinculado da unidade.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            mostrarMensagem(
                    Alert.AlertType.ERROR,
                    e.getMessage() == null
                            ? "Não foi possível desvincular o serviço."
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
            List<Servico> vinculados = unidadeDAO.listarServicosVinculados(unidade.getId());
            listaServicosVinculados.setItems(FXCollections.observableArrayList(vinculados));

            Set<Integer> idsVinculados = vinculados.stream()
                    .map(Servico::getId)
                    .collect(Collectors.toSet());
            cbServico.setItems(FXCollections.observableArrayList(
                    todosServicos.stream()
                            .filter(servico -> !idsVinculados.contains(servico.getId()))
                            .toList()
            ));
        } catch (RuntimeException e) {
            e.printStackTrace();
            listaServicosVinculados.getItems().clear();
            cbServico.getItems().clear();
            mostrarMensagem(
                    Alert.AlertType.ERROR,
                    e.getMessage() == null
                            ? "Não foi possível carregar os serviços desta unidade."
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
