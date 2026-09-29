package org.githubio.desktop_beleza.model;

public class Unidade {

    private int id;
    private String nome;
    private String endereco;
    private String situacao;

    public Unidade() {
    }

    public Unidade(int id, String nome, String endereco, String situacao) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.situacao = situacao;
    }

    public Unidade(String nome, String endereco, String situacao) {
        this.nome = nome;
        this.endereco = endereco;
        this.situacao = situacao;
    }

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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getStatus() {
        return "A".equalsIgnoreCase(situacao)
                ? "Ativa"
                : "Desativada";
    }
}