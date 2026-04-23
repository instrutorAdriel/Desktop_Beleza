CREATE DATABASE db_salao_beleza;
USE db_salao_beleza;

CREATE TABLE tb_instrutores (
	id_instrutor INTEGER PRIMARY KEY AUTO_INCREMENT,
    email_instrutor VARCHAR(64) UNIQUE NOT NULL,
    senha VARCHAR(128) NOT NULL
);

CREATE TABLE tb_turmas (
	id_turma INTEGER PRIMARY KEY AUTO_INCREMENT,
    turno ENUM("Matutino", "Vespertino", "Noturno"),
    id_status_turma INTEGER,
    FOREIGN KEY (id_status_turma) REFERENCES tb_status_turma(id_status_turma)
);

CREATE TABLE tb_modelos (
	id_modelos INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome_modelo VARCHAR(96) NOT NULL,
    email VARCHAR(64) UNIQUE,
    telefone INT(10)
);

CREATE TABLE tb_servicos (
	id_servico INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome_servico VARCHAR(64) NOT NULL,
    descricao VARCHAR(255),
    horario_disponivel TIME
);

CREATE TABLE tb_agenda (
	id_agenda INTEGER PRIMARY KEY AUTO_INCREMENT,
    id_servico INTEGER,
    id_turmas_instrutores INTEGER,
    id_modelo INTEGER,
    data_agenda DATE NOT NULL,
    horario_agenda TIME NOT NULL,
    id_status_agenda INTEGER,
    FOREIGN KEY (id_servico) REFERENCES tb_servicos(id_servico),
    FOREIGN KEY (id_turmas_instrutores) REFERENCES rl_turmas_instrutores(id_turmas_instrutores),
    FOREIGN KEY (id_modelo) REFERENCES tb_modelos(id_modelos),
    FOREIGN KEY (id_status_agenda) REFERENCES tb_status_agenda(id_status_agenda)
);

CREATE TABLE rl_turmas_instrutores (
	id_turmas_instrutores INTEGER PRIMARY KEY AUTO_INCREMENT,
    id_instrutor INTEGER,
    id_turma INTEGER,
    FOREIGN KEY (id_instrutor) REFERENCES tb_instrutores(id_instrutor),
    FOREIGN KEY (id_turma) REFERENCES tb_turmas(id_turma)
);

# Status da turma: Em Andamento, Finalizado
CREATE TABLE tb_status_turma ( 
	id_status_turma INTEGER PRIMARY KEY AUTO_INCREMENT,
    status_turma VARCHAR(24) NOT NULL
);

# Status da Agenda: Pendente, Compareceu, Não Compareceu
CREATE TABLE tb_status_agenda (
	id_status_agenda INTEGER PRIMARY KEY AUTO_INCREMENT,
    status_agenda VARCHAR(24) NOT NULL
);

# Inserção dos dados
# Dados pré-definidos nas tabelas tb_status_agenda e tb_status_turma
INSERT INTO tb_status_agenda(id_status_agenda, status_agenda) VALUES 
(1, "Pendente"),
(2, "Compareceu"),
(3, "Não Compareceu");

INSERT INTO tb_status_turma(id_status_turma, status_turma) VALUES 
(1, "Em Andamento"),
(2, "Finalizado");

# Dados do usuário admnistrador
INSERT INTO tb_instrutores(id_instrutor, email_instrutor, senha) VALUES
(1, "Admin@df.senac.br", "Admin123");