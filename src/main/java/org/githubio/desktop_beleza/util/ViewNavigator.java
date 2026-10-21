package org.githubio.desktop_beleza.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.githubio.desktop_beleza.MainApplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewNavigator {

    private static StackPane contentArea;
    // Cache mantido na memória durante toda a execução da aplicação
    private static final Map<String, Node> viewCache = new HashMap<>();

    public static void setContentArea(StackPane area) {
        contentArea = area;
    }

    public static void loadView(String fxmlPath) {
        if (contentArea == null) {
            System.err.println("Área de conteúdo não configurada no ViewNavigator!");
            return;
        }

        try {
            Node targetView;

            // Se a tela já está no cache, reaproveita a instância idêntica com todos os dados
            if (viewCache.containsKey(fxmlPath)) {
                targetView = viewCache.get(fxmlPath);
            } else {
                // Primeira vez: carrega o FXML e armazena no cache permanente
                FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
                targetView = loader.load();
                viewCache.put(fxmlPath, targetView);
            }

            if (!contentArea.getChildren().contains(targetView)) {
                contentArea.getChildren().add(targetView);
            }

            // Apenas alterna a visibilidade sem recarregar o controller ou o banco
            for (Node child : contentArea.getChildren()) {
                child.setVisible(child == targetView);
                child.setManaged(child == targetView);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao carregar a vista: " + fxmlPath);
        }
    }

    /**
     * Chamado apenas se quiser forçar a limpeza total ao fechar/sair do sistema.
     */
    public static void clearCache() {
        viewCache.clear();
        if (contentArea != null) {
            contentArea.getChildren().clear();
        }
    }
}