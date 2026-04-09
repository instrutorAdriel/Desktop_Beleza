package org.githubio.desktop_beleza.model;

public class Servico {
    private int id;
    private String nome;
    private String descricao;
    private String horario;

    public Servico(String nome, String descricao, String horario) {
        this.nome = nome;
        this.descricao = descricao;
        this.horario = horario;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}