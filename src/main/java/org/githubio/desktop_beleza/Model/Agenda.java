package org.githubio.desktop_beleza.model;

public class Agenda {
    private int id;
    private String data;
    private String servico;
    private String pratica;
    private String cliente;
    private String status;
    private String horario;

    // Construtor ajustado para a ordem da DAO: id, pratica, data, horario, status, cliente, servico
    public Agenda(int id, String pratica, String data, String horario, String status, String cliente, String servico) {
        this.id = id;
        this.pratica = pratica;
        this.data = data;
        this.horario = horario;
        this.status = status;
        this.cliente = cliente;
        this.servico = servico;
    }

    // Getters e Setters (Padrão JavaBeans para o JavaFX reconhecer)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getServico() { return servico; }
    public void setServico(String servico) { this.servico = servico; }

    public String getPratica() { return pratica; }
    public void setPratica(String pratica) { this.pratica = pratica; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}