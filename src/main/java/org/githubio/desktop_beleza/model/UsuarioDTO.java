package org.githubio.desktop_beleza.model;

public class UsuarioDTO {
    private int idTurma; // CAMPO NOVO
    private String turma;
    private String turno;
    private String nomeInstrutor;
    private String statusTurma;

    // Atualize o construtor para receber o ID também
    public UsuarioDTO(int idTurma, String turma, String turno, String nomeInstrutor, String statusTurma) {
        this.idTurma = idTurma;
        this.turma = turma;
        this.turno = turno;
        this.nomeInstrutor = nomeInstrutor;
        this.statusTurma = statusTurma;
    }

    // Adicione o Getter e Setter para o idTurma
    public int getIdTurma() { return idTurma; }
    public void setIdTurma(int idTurma) { this.idTurma = idTurma; }
    public String getTurma() { return turma; }
    public String getTurno() { return turno; }
    public String getNomeInstrutor() { return nomeInstrutor; }
    public String getStatusTurma() {return statusTurma;}


    public void setTurma(String turma) {
        this.turma = turma;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public void setNomeInstrutor(String nomeInstrutor) {
        this.nomeInstrutor = nomeInstrutor;
    }

    public void setStatusTurma(String statusTurma) {this.statusTurma = statusTurma;}



}
