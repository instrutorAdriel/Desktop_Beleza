package org.githubio.desktop_beleza.model;

public class Agenda {
    private int id;
    private String data;
    private String servico;

    private String modelo;
    private String status;
    private String horario;

    // Construtor ajustado para a ordem da DAO: id, pratica, data, horario, status, modelo, servico
    public Agenda(int id, String data, String horario, String status, String modelo, String servico) {
        this.id = id;
        this.data = data;
        this.horario = horario;
        this.status = status;
        this.modelo = modelo;
        this.servico = servico;
    }

    // Getters e Setters (Padrão JavaBeans para o JavaFX reconhecer)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getServico() { return servico; }
    public void setServico(String servico) { this.servico = servico; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}