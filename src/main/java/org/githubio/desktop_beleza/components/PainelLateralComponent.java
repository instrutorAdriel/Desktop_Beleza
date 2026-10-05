package org.githubio.desktop_beleza.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import org.githubio.desktop_beleza.MainApplication;

import java.io.IOException;

public class PainelLateralComponent extends VBox {

    public PainelLateralComponent() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/githubio/desktop_beleza/painelateral.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException("Erro ao carregar o painel lateral", exception);
        }
    }

    @FXML
    public void trocarTelaParaPaginaInicial() throws IOException {
        MainApplication.setRoot("Telaagenda");
    }

    @FXML
    public void trocarTelaParaModelos() throws IOException {
        MainApplication.setRoot("gerenciarmodelo");
    }

    @FXML
    public void trocarTelaParaServicos() throws IOException {
        MainApplication.setRoot("servicos");
    }

    @FXML
    public void trocarTelaParaUnidades() throws IOException {
        MainApplication.setRoot("unidades");
    }

    @FXML
    public void trocarTelaParaTurmas() throws IOException {
        MainApplication.setRoot("GerenciarTurma");
    }

    @FXML
    public void sairDoSistema() throws IOException {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja sair do sistema?", ButtonType.YES, ButtonType.NO);
        if (alerta.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            MainApplication.setUsuario("");
            MainApplication.setRoot("login");
        }
    }
}