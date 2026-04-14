package org.githubio.desktop_beleza.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.githubio.desktop_beleza.GerenciarTurmaApplication;
import org.githubio.desktop_beleza.model.GerenciarTurmaDAO;
import org.githubio.desktop_beleza.model.UsuarioDTO;

import java.io.IOException;
import java.util.List;


public class GerenciarTurmaController {
    @FXML
    private TableView<UsuarioDTO> tabelaUsuarios;
    @FXML
    private TableColumn<UsuarioDTO, String> colTurma;
    @FXML
    private TableColumn<UsuarioDTO, String> colTurno;
    @FXML
    private TableColumn<UsuarioDTO, String> colInstrutor;
    @FXML
    private TableColumn<UsuarioDTO, Void> colAcoes;
    @FXML
    private TextField txtBusca;

    private final ObservableList<UsuarioDTO> listaOriginal = FXCollections.observableArrayList();

    public void initialize() {
        // 1. Vincula as colunas (Seu código original)
        colTurma.setCellValueFactory(new PropertyValueFactory<>("turma"));
        colTurno.setCellValueFactory(new PropertyValueFactory<>("turno"));
        colInstrutor.setCellValueFactory(new PropertyValueFactory<>("nomeInstrutor"));

        // 2. Configura a coluna de ações (Seu código original)
        colAcoes.setCellFactory(coluna -> new TableCell<>() {
            private final ChoiceBox<String> choiceBox = new ChoiceBox<>();
            {
                choiceBox.getItems().addAll("Editar", "Excluir");
                choiceBox.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
                    if (novo == null) return;
                    UsuarioDTO usuario = getTableView().getItems().get(getIndex());
                    GerenciarTurmaDAO dao = new GerenciarTurmaDAO();

                    if (novo.equals("Excluir")) {
                        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, "Excluir " + usuario.getTurma() + "?", ButtonType.YES, ButtonType.NO);
                        alerta.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.YES) {
                                // 1. Remove do Banco de Dados
                                dao.excluirTurma(usuario.getTurma());

                                System.out.println("ID selecionado para excluir: " + usuario.getIdTurma());
                                System.out.println("IDs presentes na listaOriginal: ");
                                listaOriginal.forEach(u -> System.out.print(u.getIdTurma() + " "));

                                // 2. O PULO DO GATO: Remove da lista que a tabela está usando
                                // Como a SortedList/FilteredList estão "escutando" a listaOriginal,
                                // ao remover daqui, a tabela atualiza na hora!
                                //listaOriginal.remove(usuario);
                                IO.println(listaOriginal);

                                // Caso ainda não suma, force o refresh (opcional)
                                tabelaUsuarios.refresh();
                                try {
                                    GerenciarTurmaApplication.setRoot("gerenciarTurma");
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                        });
                    } else if (novo.equals("Editar")) {
                        abrirJanelaEdicao(usuario);
                    }
                    javafx.application.Platform.runLater(() -> choiceBox.getSelectionModel().clearSelection());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : choiceBox);
            }
        });

        // --- AQUI COMEÇA A PARTE DA BUSCA ---

        // 3. Carrega os dados do banco em uma lista observável
        GerenciarTurmaDAO dao = new GerenciarTurmaDAO();
        ObservableList<UsuarioDTO> listaOriginal = dao.lerUsuariosParaTabela();

        // 4. Cria a FilteredList baseada na lista original
        FilteredList<UsuarioDTO> listaFiltrada = new FilteredList<>(listaOriginal, p -> true);

        // 5. Conecta o TextField à FilteredList
        txtBusca.textProperty().addListener((observable, oldValue, newValue) -> {
            listaFiltrada.setPredicate(usuario -> {
                // Se o campo estiver vazio, mostra tudo
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String filtro = newValue.toLowerCase();

                // Busca em todas as colunas
                if (usuario.getTurma().toLowerCase().contains(filtro)) return true;
                if (usuario.getTurno().toLowerCase().contains(filtro)) return true;
                if (usuario.getNomeInstrutor().toLowerCase().contains(filtro)) return true;

                return false; // Não combina
            });
        });

        // 6. Envolve em uma SortedList para que a ordenação das colunas (A-Z) continue funcionando
        SortedList<UsuarioDTO> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tabelaUsuarios.comparatorProperty());

        // 7. Seta os itens na tabela (Usando a lista que passou pelo filtro e pela ordenação)
        tabelaUsuarios.setItems(listaOrdenada);


    }

    private void abrirJanelaEdicao(UsuarioDTO usuario) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Turma Completa");
        dialog.setHeaderText("Atualize os dados da " + usuario.getTurma());

        // 1. Campos de texto e Turno
        TextField txtTurma = new TextField(usuario.getTurma());
        ComboBox<String> cbTurno = new ComboBox<>();
        cbTurno.getItems().addAll("Matutino", "Vespertino", "Noturno");
        cbTurno.setValue(usuario.getTurno());

        // 2. ComboBox de Instrutores (Carregando do banco)
        ComboBox<String> cbInstrutor = new ComboBox<>();
        GerenciarTurmaDAO dao = new GerenciarTurmaDAO();

        // Busca a lista de nomes e adiciona no ComboBox
        List<String> listaNomes = dao.listarNomesInstrutores();
        cbInstrutor.getItems().addAll(listaNomes);

        // Define o instrutor atual como selecionado
        cbInstrutor.setValue(usuario.getNomeInstrutor());

        // 3. Layout da Janela
        VBox layout = new VBox(10,
                new Label("Nome da Turma:"), txtTurma,
                new Label("Turno:"), cbTurno,
                new Label("Instrutor:"), cbInstrutor
        );
        layout.setPadding(new javafx.geometry.Insets(20));

        dialog.getDialogPane().setContent(layout);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 4. Salva no Banco usando o ID da Turma
                dao.atualizarCompleto(
                        usuario.getIdTurma(),
                        txtTurma.getText(),
                        cbTurno.getValue(),
                        cbInstrutor.getValue() // Pega o nome selecionado no combo
                );

                // 5. Atualiza o objeto na memória (UI)
                usuario.setTurma(txtTurma.getText());
                usuario.setTurno(cbTurno.getValue());
                usuario.setNomeInstrutor(cbInstrutor.getValue());

                tabelaUsuarios.refresh();
            }
        });
    }

}