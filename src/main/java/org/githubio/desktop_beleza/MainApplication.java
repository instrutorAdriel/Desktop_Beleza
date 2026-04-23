package org.githubio.desktop_beleza;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    private static Scene scene;
    private static final int MinWidth = 1366;
    private static final int MinHeight = 768;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("gerenciarmodelo"), MinWidth, MinHeight);
       // A primeira tela a ser exibida é a de Login

        stage.setTitle("Desktop Beleza");
        stage.setScene(scene);
        stage.setMinWidth(MinWidth);
        stage.setMinHeight(MinHeight);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
}
