package Model;

public class Agenda {
    private int id;
    private String data;
    private String servico;
    private String pratica;
    private String cliente;
    private String status;

    public Agenda(int id, String data, String servico, String pratica, String cliente, String status) {
        this.id = id;
        this.data = data;
        this.servico = servico;
        this.pratica = pratica;
        this.cliente = cliente;
        this.status = status;
    }

    // Getters e Setters
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

    // Não Funciona ainda
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
