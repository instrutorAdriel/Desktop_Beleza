package org.githubio.desktop_beleza.model;

public class Servico {
    private int id;
    private String nome;
    private String descricao;
    private String duracao;

    // Construtor completo (útil para o DAO)
    public Servico(int id, String nome, String descricao, String duracao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.duracao = duracao;
    }

    // Construtor sem ID (útil para novos cadastros antes de irem ao banco)
    public Servico(String nome, String descricao, String duracao) {
        this.nome = nome;
        this.descricao = descricao;
        this.duracao = duracao;
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

    public String getDuracao() {
        return duracao;
    }

    public void setDuracao(String duracao) {
        this.duracao = duracao;
    }

    // Opcional: Sobrescrever o toString ajuda no debug
    @Override
    public String toString() {
        return "Servico{" + "id=" + id + ", nome='" + nome + '\'' + '}';
    }
}