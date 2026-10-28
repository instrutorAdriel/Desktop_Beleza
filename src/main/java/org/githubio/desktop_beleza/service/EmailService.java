package org.githubio.desktop_beleza.service;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.Session.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {
    private final String remetente;
    private final String senhaApp;

    public EmailService() {
        Dotenv dotenv = Dotenv.load();
        this.remetente = dotenv.get("GMAIL_USER");
        this.senhaApp = dotenv.get("GMAIL_APP_PASSWORD");

        if (remetente == null || senhaApp == null) {
            throw new IllegalStateException("Falha ao criar serviço de e-mail.");
        }
    }

    public void enviarCodigoRecuperacao(String emailDestino, String codigoOtp) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remetente, senhaApp);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remetente, "SENAC Beleza - Suporte"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(emailDestino));
            message.setSubject("Código de Recuperação de Senha - SENAC Beleza");

            String corpoHtml = """
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2 style="color: #004a8d;">Recuperação de Senha</h2>
                    <p>Você solicitou a redefinição de senha para o seu acesso ao sistema <strong>SENAC Beleza</strong>.</p>
                    <p>Utilize o código de verificação abaixo para continuar:</p>
                    <div style="background-color: #f7941d; color: #ffffff; font-size: 24px; font-weight: bold; 
                                padding: 15px; text-align: center; border-radius: 8px; width: 200px; letter-spacing: 4px;">
                        %s
                    </div>
                    <p style="margin-top: 20px; font-size: 12px; color: #777;">
                        Este código é válido por 10 minutos. Se você não solicitou esta redefinição, ignore este e-mail.
                    </p>
                </div>
                """.formatted(codigoOtp);

            message.setContent(corpoHtml, "text/html; charset=utf-8");

            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao enviar e-mail: " + e.getMessage(), e);
        }
    }
}
