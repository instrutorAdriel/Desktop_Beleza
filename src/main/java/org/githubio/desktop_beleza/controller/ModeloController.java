package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.ModeloDAO;

import java.io.IOException;

public class ModeloController {
    @FXML
    private TextField campo_nome;
    @FXML
    private TextField campo_telefone;
    @FXML
    private TextField campo_email;
    @FXML
    public void onAdicionarmodelo(){
        ModeloDAO modeloDAO = new ModeloDAO();
        String email = campo_email.getText();
        String telefone = campo_telefone.getText();
        String nome = campo_nome.getText();
        modeloDAO.cadastrarModelo(nome, telefone, email);
    }

    @FXML
    void abrirTelaAdcionarPopUp() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("telamodelo.fxml"));

            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setTitle("tela adicionar modelo");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);


            // Quando o Pop-up fechar, recarregamos a lista original do banco
            stage.setOnHiding(event -> onAdicionarmodelo());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
