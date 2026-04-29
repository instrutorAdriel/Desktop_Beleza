package org.githubio.desktop_beleza.model;

public class Modelo {
    private int id;
    private String nome;
    private String telefone;
    private String email;

    // 1. Construtor vazio (Importante para o JavaFX e instâncias novas)
    public Modelo() {}

    // 2. NOVO Construtor: Usado pela sua nova DAO (sem o ID no início)
    public Modelo(String nome, String telefone, String email) {
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
    }

    // 3. Construtor completo: Útil se precisar criar um objeto com tudo de uma vez
    public Modelo(int id, String nome, String telefone, String email) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
    }

    // --- Getters e Setters ---
    // Importante: O PropertyValueFactory("nome") procura o método getNome()

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}