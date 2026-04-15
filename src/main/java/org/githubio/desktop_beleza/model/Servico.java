package org.githubio.desktop_beleza.model;

public class Servico {
    private int id;
    private String nome;
    private String descricao;
    private String horario;

    // Construtor completo (útil para o DAO)
    public Servico(int id, String nome, String descricao, String horario) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.horario = horario;
    }

    // Construtor sem ID (útil para novos cadastros antes de irem ao banco)
    public Servico(String nome, String descricao, String horario) {
        this.nome = nome;
        this.descricao = descricao;
        this.horario = horario;
    }

    // Construtor vazio (exigido por algumas bibliotecas e boa prática)
    public Servico() {
    }

    // Getters e Setters (essenciais para a TableView funcionar)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    // Opcional: Sobrescrever o toString ajuda no debug
    @Override
    public String toString() {
        return "Servico{" + "id=" + id + ", nome='" + nome + '\'' + '}';
    }
}