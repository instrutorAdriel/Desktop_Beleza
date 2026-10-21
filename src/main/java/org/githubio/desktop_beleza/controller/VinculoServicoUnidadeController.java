package org.githubio.desktop_beleza.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.githubio.desktop_beleza.model.Servico;
import org.githubio.desktop_beleza.model.Unidade;
import org.githubio.desktop_beleza.model.UnidadeDAO;

import java.util.List;

public class VinculoServicoUnidadeController {

    @FXML
    private ComboBox<Unidade> cbUnidade;

    @FXML
    private ComboBox<Servico> cbServico;

    private final UnidadeDAO unidadeDAO = new UnidadeDAO();


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
    }


    // Chamados pela tela de Unidades antes de abrir a janela
    public void setUnidades(List<Unidade> unidades) {
        cbUnidade.setItems(FXCollections.observableArrayList(unidades));
    }

    public void setServicos(List<Servico> servicos) {
        cbServico.setItems(FXCollections.observableArrayList(servicos));
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

            fechar();

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
