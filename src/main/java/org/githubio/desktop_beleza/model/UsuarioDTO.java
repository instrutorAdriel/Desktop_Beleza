package org.githubio.desktop_beleza.model;

public class UsuarioDTO {
    private int idTurma; // CAMPO NOVO
    private String turma;
    private String turno;
    private String nomeInstrutor;

    // Atualize o construtor para receber o ID também
    public UsuarioDTO(int idTurma, String turma, String turno, String nomeInstrutor) {
        this.idTurma = idTurma;
        this.turma = turma;
        this.turno = turno;
        this.nomeInstrutor = nomeInstrutor;
    }

    // Adicione o Getter e Setter para o idTurma
    public int getIdTurma() { return idTurma; }
    public void setIdTurma(int idTurma) { this.idTurma = idTurma; }
    public String getTurma() { return turma; }
    public String getTurno() { return turno; }
    public String getNomeInstrutor() { return nomeInstrutor; }


    public void setTurma(String turma) {
        this.turma = turma;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public void setNomeInstrutor(String nomeInstrutor) {
        this.nomeInstrutor = nomeInstrutor;
    }



}
