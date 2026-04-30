package org.githubio.desktop_beleza.controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.Modelo;
import org.githubio.desktop_beleza.model.ModeloDAO;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ModeloController implements Initializable {

    @FXML private TableView<Modelo> tabelaModelos;
    @FXML private TableColumn<Modelo, String> colNome;
    @FXML private TableColumn<Modelo, String> colTelefone;
    @FXML private TableColumn<Modelo, String> colEmail;
    @FXML private TableColumn<Modelo, Void> colAcoes;
    @FXML private TextField txtBuscar;

    private final ModeloDAO dao = new ModeloDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configura as colunas para lerem os atributos da classe Modelo
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Configura a coluna de ações (botões)
        colAcoes.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
        configurarColunaAcoes();

        // Carrega os dados na inicialização
        atualizarTabela();
    }

    // Método atualizado conforme sua solicitação
    private void atualizarTabela() {
        tabelaModelos.setItems(
                FXCollections.observableArrayList(dao.lerTodos())
        );
    }

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button("");
            private final Button btnDel = new Button("");
            private final HBox container = new HBox(10, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().add("editar");
                btnDel.getStyleClass().add("excluir");
                container.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> abrirDialogoEdicao(getTableRow().getItem()));
                btnDel.setOnAction(e -> confirmarExclusao(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void abrirDialogoEdicao(Modelo modelo) {
        if (modelo == null) return;

        boolean isNovo = (modelo.getId() == 0);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isNovo ? "Novo Modelo" : "Editar Modelo");

        ButtonType btnSalvarType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvarType, ButtonType.CANCEL);

        TextField txtNome = new TextField(modelo.getNome());
        TextField txtTel = new TextField(modelo.getTelefone());
        TextField txtEmail = new TextField(modelo.getEmail());

        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Nome:"), txtNome,
                new Label("Telefone:"), txtTel,
                new Label("Email:"), txtEmail));

        // Obtemos o botão real para aplicar a lógica de validação sem fechar o diálogo
        final Button btnSalvar = (Button) dialog.getDialogPane().lookupButton(btnSalvarType);

        btnSalvar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String telRaw = txtTel.getText().replaceAll("\\D", ""); // Remove tudo que não é número
            String email = txtEmail.getText();

            // 1. Validação do Telefone (Tamanho e Tipo)
            if (telRaw.length() > 10) {
                mostrarAlerta("Erro no Telefone", "O telefone não pode ter mais de 10 dígitos.");
                event.consume(); // Impede o diálogo de fechar
                return;
            }

            // 2. Validação simples de Email
            if (!email.contains("@") || !email.contains(".")) {
                mostrarAlerta("Erro no Email", "Por favor, insira um e-mail válido.");
                event.consume();
                return;
            }

            // Se passar nas validações, atualiza o objeto
            modelo.setNome(txtNome.getText());
            modelo.setTelefone(telRaw);
            modelo.setEmail(email);

            if (isNovo) {
                dao.cadastrar(modelo);
            } else {
                dao.atualizar(modelo);
            }
            atualizarTabela();
        });

        dialog.showAndWait();
    }

    // Método auxiliar para mostrar os avisos
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void confirmarExclusao(Modelo modelo) {
        if (modelo == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Excluir " + modelo.getNome() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                dao.excluir(modelo.getId());
                atualizarTabela(); // Recarrega a tabela após excluir
            }
        });
    }

    @FXML
    private void abrirTelaAdcionarPopUp() {
        abrirDialogoEdicao(new Modelo());
    }

    @FXML
    private void buscar() {
        String t = txtBuscar.getText().toLowerCase();
        if (t.isEmpty()) {
            atualizarTabela();
        } else {
            tabelaModelos.setItems(FXCollections.observableArrayList(
                    dao.lerTodos().stream()
                            .filter(m -> m.getNome().toLowerCase().contains(t) ||
                                    m.getEmail().toLowerCase().contains(t))
                            .toList()
            ));
        }
    }

    // Metodos para trocas de telas
    @FXML
    public void trocarTelaParaServicos() throws IOException {
        MainApplication.setRoot("servicos");
    }

    @FXML
    public void trocarTelaParaTurmas() throws IOException{
        MainApplication.setRoot("GerenciarTurma");
    }

    @FXML
    public void trocarTelaParaPaginaInicial() throws IOException{
        MainApplication.setRoot("");
    }

    @FXML
    public void sairDoSistema() throws IOException {
        // Desenvolver uma tela de dialogo pergunta se o usuário deseja sair do sistema e retornar para tela de login
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Sair do Sistema");
        alerta.setHeaderText(null);
        alerta.setContentText("Você deseja do sair do sistema?");

        // Botões de SIM e NÃO
        ButtonType botaoSim = new ButtonType("SIM");
        ButtonType botaoNao = new ButtonType("NÃO");

        alerta.getButtonTypes().setAll(botaoSim, botaoNao);

        if (alerta.showAndWait().get() == botaoSim) {
            MainApplication.setUsuario("");
            MainApplication.setRoot("login");
        }
    }
}