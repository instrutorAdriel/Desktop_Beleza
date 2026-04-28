package org.githubio.desktop_beleza.model;


public class Modelo {
    private int id;
    private String nome;
    private String telefone;
    private String email;

    // Construtor vazio
    public Modelo() {}

    // Construtor completo
    public Modelo(int id, String nome, String telefone, String email) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
    }

    // Getters e Setters (Nomes devem bater com o PropertyValueFactory do Controller)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}