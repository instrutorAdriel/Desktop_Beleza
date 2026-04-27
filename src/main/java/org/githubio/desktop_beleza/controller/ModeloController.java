package org.githubio.desktop_beleza.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.githubio.desktop_beleza.model.ModeloDAO;

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
}
