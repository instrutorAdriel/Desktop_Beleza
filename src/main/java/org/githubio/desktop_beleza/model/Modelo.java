package org.githubio.desktop_beleza.model;

import java.sql.Date;

public class Modelo {
    private int id;
    private String nome;
    private String telefone;
    private String email;
    private Date data_nascimento;

    public Modelo() {}

    public Modelo(String nome, String telefone, String email, Date data_nascimento) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.data_nascimento = data_nascimento;
    }

    public Modelo(int id, String nome, String telefone, String email, Date data_nascimento) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.data_nascimento = data_nascimento;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Date getData_nascimento() {return data_nascimento;}
    public void setData_nascimento(Date data_nascimento) {this.data_nascimento = data_nascimento;}
}