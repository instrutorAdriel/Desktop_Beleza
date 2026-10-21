package org.githubio.desktop_beleza.controller;

import org.githubio.desktop_beleza.util.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class MainLayoutController {

    @FXML
    private StackPane mainContentArea;

    @FXML
    public void initialize() {
        ViewNavigator.setContentArea(mainContentArea);
        // Define a tela inicial padrão do layout
        ViewNavigator.loadView("Telaagenda.fxml");
    }
}