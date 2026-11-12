# Recuperação de senha

O código de recuperação é enviado para o endereço de e-mail cadastrado na tabela `usuario`; durante os testes, esse endereço também pode ser Gmail. A caixa de entrada ser Outlook ou Gmail não exige configuração especial no destinatário: o SMTP configurado abaixo é o serviço usado para enviar a mensagem.

## Configuração do remetente

O envio continua compatível com as variáveis Gmail já usadas pelo projeto (`GMAIL_USER` e `GMAIL_APP_PASSWORD`). Para enviar por uma conta Microsoft 365/Outlook, configure no `.env`:

```dotenv
SMTP_HOST=smtp.office365.com
SMTP_PORT=587
SMTP_USER=conta.remetente@df.senac.br
SMTP_PASSWORD=senha-ou-senha-de-aplicativo
```

No Microsoft 365, o SMTP autenticado precisa estar habilitado para a conta remetente; a política da instituição pode exigir autorização do administrador. Não use a senha pessoal da caixa postal no repositório. Se a conta do usuário apenas recebe o código, a configuração Gmail legada pode continuar sendo usada para enviar para o Outlook.

## Banco de dados

Execute uma vez em MySQL, se a tabela ainda não existir:

```sql
CREATE TABLE IF NOT EXISTS passwordresettoken (
    id_token BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    token CHAR(6) NOT NULL,
    data_expiracao DATETIME NOT NULL,
    PRIMARY KEY (id_token),
    INDEX idx_passwordresettoken_usuario_expiracao (id_usuario, data_expiracao),
    CONSTRAINT fk_passwordresettoken_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;
```

O código expira em 10 minutos e é removido do banco assim que validado, portanto só pode ser usado uma vez.
