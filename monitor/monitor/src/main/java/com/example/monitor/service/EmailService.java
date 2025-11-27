package com.example.monitor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Redefinição de senha - MonitorHidrico");

            String htmlContent = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f7f9fc; padding: 20px;">
                <div style="max-width: 500px; margin: auto; background-color: #fff; border-radius: 10px; padding: 30px; box-shadow: 0 0 10px rgba(0,0,0,0.1);">
                    <h2 style="color: #0066cc;">Redefinição de Senha</h2>
                    <p>Olá,</p>
                    <p>Para redefinir sua senha, use o seguinte token:</p>
                    <h3 style="background: #0066cc; color: white; padding: 10px; border-radius: 5px; text-align: center;">%s</h3>
                    <p>Ou clique no botão abaixo para redefinir diretamente:</p>
                    <p style="text-align: center;">
                        <a href="http://localhost:8081/reset-password.html?token=%s"
                           style="background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                            Redefinir senha
                        </a>
                    </p>
                    <hr style="border:none; border-top: 1px solid #eee; margin:20px 0;">
                    <p style="font-size: 12px; color: #777;"> Se você não solicitou esta redefinição, ignore este email.</p>
                </div>
                </body>
                </html>
                """.formatted(token, token);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error so enviar e-mail", e);
        }
    }
}