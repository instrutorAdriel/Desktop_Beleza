package org.githubio.desktop_beleza.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.githubio.desktop_beleza.MainApplication;
import org.githubio.desktop_beleza.model.GerenciarTurmaDAO;
import org.githubio.desktop_beleza.model.UsuarioDTO;


import java.io.IOException;

public class GerenciarTurmaController {
    @FXML private TableView<UsuarioDTO> tabelaUsuarios;
    @FXML private TableColumn<UsuarioDTO, String> colTurma;
    @FXML private TableColumn<UsuarioDTO, String> colTurno;
    @FXML private TableColumn<UsuarioDTO, String> colInstrutor;
    @FXML private TableColumn<UsuarioDTO, Void> colAcoes;
    @FXML private TableColumn<UsuarioDTO, String> colStatus;
    @FXML private TextField txtBusca;

    // 1. Declaramos o DAO como atributo da CLASSE (Global)
    private final GerenciarTurmaDAO dao = new GerenciarTurmaDAO();

    // 2. A lista original que o filtro vai observar
    private final ObservableList<UsuarioDTO> listaOriginal = FXCollections.observableArrayList();

    public void initialize() {
        // Vincula as colunas
        colTurma.setCellValueFactory(new PropertyValueFactory<>("turma"));
        colTurno.setCellValueFactory(new PropertyValueFactory<>("turno"));
        colInstrutor.setCellValueFactory(new PropertyValueFactory<>("nomeInstrutor"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusTurma"));

        // 1. CHAMA o método que configura as ações (Ele agora está fora deste método)
        configurarColunaAcoes();

        // 2. Carrega os dados
        atualizarTabela();

        // 3. Configuração do Filtro (Busca)
        FilteredList<UsuarioDTO> listaFiltrada = new FilteredList<>(listaOriginal, p -> true);
        txtBusca.textProperty().addListener((observable, oldValue, newValue) -> {
            listaFiltrada.setPredicate(usuario -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String filtro = newValue.toLowerCase();

                return usuario.getTurma().toLowerCase().contains(filtro) ||
                        usuario.getTurno().toLowerCase().contains(filtro) ||
                        usuario.getNomeInstrutor().toLowerCase().contains(filtro) ||
                        usuario.getStatusTurma().toLowerCase().contains(filtro);
            });
        });

        SortedList<UsuarioDTO> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tabelaUsuarios.comparatorProperty());
        tabelaUsuarios.setItems(listaOrdenada);

        tabelaUsuarios.getSortOrder().add(colTurma); // Adiciona a coluna de Turma na fila de ordenação
        colTurma.setSortType(TableColumn.SortType.ASCENDING); // Define que é de A a Z
        tabelaUsuarios.sort(); // Aplica a ordenação agora
    }

    private void configurarColunaAcoes() {
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button();
            private final Button btnExcluir = new Button();
            private final HBox container = new HBox(10, btnEditar, btnExcluir);

            {
                btnEditar.getStyleClass().add("editar");
                btnExcluir.getStyleClass().add("excluir");
                container.setAlignment(Pos.CENTER);

                btnEditar.setOnAction(event -> {
                    UsuarioDTO usuario = getTableView().getItems().get(getIndex());
                    abrirJanelaEdicao(usuario);
                });

                btnExcluir.setOnAction(event -> {
                    UsuarioDTO usuario = getTableView().getItems().get(getIndex());
                    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, "Excluir a turma " + usuario.getTurma() + "?", ButtonType.YES, ButtonType.NO);
                    alerta.setTitle("Confirmação de Exclusão");
                    alerta.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            dao.excluirTurma(usuario.getTurma());
                            listaOriginal.remove(usuario);
                        }
                    });
                });
                container.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    // Criamos um método para facilitar a atualização em vários pontos
    private void atualizarTabela() {

        listaOriginal.setAll(dao.lerUsuariosParaTabela(MainApplication.getUsuario()));
    }

    private void abrirJanelaEdicao(UsuarioDTO usuario) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Turma");

        TextField txtTurma = new TextField(usuario.getTurma());
        ComboBox<String> cbTurno = new ComboBox<>();
        cbTurno.getItems().addAll("Matutino", "Vespertino", "Noturno");
        cbTurno.setValue(usuario.getTurno());

        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Em andamento", "Finalizada");
        cbStatus.setValue(usuario.getStatusTurma());

        ComboBox<String> cbInstrutor = new ComboBox<>();
        cbInstrutor.getItems().addAll(dao.listarNomesInstrutores());
        cbInstrutor.setValue(usuario.getNomeInstrutor());

        VBox layout = new VBox(10, new Label("Turma:"), txtTurma, new Label("Turno:"), cbTurno,
                new Label("Instrutor:"), cbInstrutor, new Label("Status:"), cbStatus);
        layout.setPadding(new javafx.geometry.Insets(20));
        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dao.atualizarCompleto(usuario.getIdTurma(), txtTurma.getText(), cbTurno.getValue(),
                        cbInstrutor.getValue(), cbStatus.getValue());

                // Atualiza o objeto na lista original para refletir na TableView
                usuario.setTurma(txtTurma.getText());
                usuario.setTurno(cbTurno.getValue());
                usuario.setNomeInstrutor(cbInstrutor.getValue());
                usuario.setStatusTurma(cbStatus.getValue());
                tabelaUsuarios.refresh();
            }
        });
    }

    @FXML
    void abrirTelaCadastroPopUp() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("adicionarTurma.fxml"));

            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setWidth(1280);
            stage.setHeight(720);
            stage.setTitle("Cadastrar Nova Turma");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);


            // Quando o Pop-up fechar, recarregamos a lista original do banco
            stage.setOnHiding(event -> atualizarTabela());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}