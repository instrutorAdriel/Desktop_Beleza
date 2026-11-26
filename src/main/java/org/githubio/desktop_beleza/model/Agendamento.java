package org.githubio.desktop_beleza.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Agendamento {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private LocalDateTime data_hora_agendamento;
    private Produto produto;
    private Modelo modelo;
    private String status;
    private String horario;

    public Agendamento(int id, LocalDateTime data_hora_agendamento, String status, Modelo modelo, Produto produto) {
        this.id = id;
        this.data_hora_agendamento = data_hora_agendamento;
        this.status = status;
        this.modelo = modelo;
        this.produto = produto;
    }

    public String getDataHoraFormatada() {
        return data_hora_agendamento.format(FORMATO);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getData_hora_agendamento() { return data_hora_agendamento; }
    public void setData_hora_agendamento(LocalDateTime data_hora_agendamento) { this.data_hora_agendamento = data_hora_agendamento; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public Modelo getModelo() { return modelo; }
    public void setModelo(Modelo modelo) { this.modelo = modelo; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}