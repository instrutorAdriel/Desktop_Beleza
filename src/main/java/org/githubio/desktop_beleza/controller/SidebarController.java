package org.githubio.desktop_beleza.controller;

import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.util.ViewNavigator;
import javafx.fxml.FXML;

import java.io.IOException;

public class SidebarController {

    @FXML public void navegarAgenda() {
        ViewNavigator.loadView("VisaoGeralAgendamentoView.fxml");
    }

    @FXML public void navegarTurmas() {
        ViewNavigator.loadView("GerenciamentoTurmasView.fxml");
    }

    @FXML public void navegarServicos() {
        ViewNavigator.loadView("ServicosView.fxml");
    }

    @FXML public void navegarModelos() {
        ViewNavigator.loadView("GerenciamentoModeloView.fxml");
    }

    @FXML public void logout() {
        ViewNavigator.clearCache();
        MainApplication.setUsuario(null); // ADICIONAR ESTA LINHA
        try {
            MainApplication.setRoot("LoginView");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}