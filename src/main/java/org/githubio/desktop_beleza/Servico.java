package org.example.demo;



public class Servico {
    private String nome;
    private String descricao;
    private String horario;

    public Servico(String nome, String descricao, String horario) {
        this.nome = nome;
        this.descricao = descricao;
        this.horario = horario;
    }

    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getHorario() { return horario; }

    public void setNome(String nome) { this.nome = nome; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setHorario(String horario) { this.horario = horario; }
}