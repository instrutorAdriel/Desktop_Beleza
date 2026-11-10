package org.githubio.desktop_beleza;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.concurrent.Task;                 // NOVO: usado no setRootAsync
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;            // NOVO: usado no setRootAsync

public class MainApplication extends Application {

    // =====================================================================
    // 1) CONSTANTES E ESTADO GLOBAL
    // =====================================================================

    private static Scene scene;
    private static final int MinWidth = 1366;
    private static final int MinHeight = 768;

    // Usuário logado
    private static String usuario = "";

    // =====================================================================
    // 2) INÍCIO DO APP
    // =====================================================================

    @Override
    public void start(Stage stage) throws IOException {
        // Tema base (antes estava duplicado, agora só uma vez)
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // A primeira tela exibida é a de Login
        scene = new Scene(loadFXML("login"), MinWidth, MinHeight);

        // CSS do Senac (antes estava sendo adicionado duas vezes)
        String cssSenac = MainApplication.class.getResource("css/style.css").toExternalForm();
        scene.getStylesheets().add(cssSenac);

        // Logo como ícone da janela
        Image appIcon = new Image(MainApplication.class.getResource("images/icon.png").toString());
        stage.getIcons().add(appIcon);

        stage.setTitle("Desktop Beleza");
        stage.setScene(scene);
        stage.setMinWidth(MinWidth);
        stage.setMinHeight(MinHeight);
        stage.show();
    }

    // =====================================================================
    // 3) TROCA DE TELA
    // =====================================================================

    // Troca de tela SÍNCRONA (a original). Boa para telas leves.
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    // NOVO: troca de tela ASSÍNCRONA. Carrega o FXML (e o initialize() do controller)
    // em background e só troca o root na thread da UI. Assim o popup de loading
    // continua animando enquanto a tela nova é montada.
    //   aoConcluir -> roda depois que a tela nova já está visível (ex.: fechar o popup)
    //   aoFalhar   -> roda se o carregamento der erro
    public static void setRootAsync(String fxml, Runnable aoConcluir, Consumer<Throwable> aoFalhar) {
        Task<Parent> tarefa = new Task<>() {
            @Override
            protected Parent call() throws IOException {
                return loadFXML(fxml); // background: NÃO mexe na scene aqui
            }
        };

        // Terminou o carregamento: já estamos na thread da UI, então pode trocar o root
        tarefa.setOnSucceeded(e -> {
            scene.setRoot(tarefa.getValue());
            if (aoConcluir != null) aoConcluir.run();
        });

        tarefa.setOnFailed(e -> {
            if (aoFalhar != null) aoFalhar.accept(tarefa.getException());
        });

        Thread t = new Thread(tarefa);
        t.setDaemon(true);
        t.start();
    }

    // Lê o FXML da pasta view/ e devolve a tela montada
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("view/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    // =====================================================================
    // 4) GETTERS E SETTERS
    // =====================================================================

    public static String getUsuario() {
        return usuario;
    }

    public static void setUsuario(String usuario) {
        MainApplication.usuario = usuario;
    }
}