package com.projet.notification.service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class EmailService {
    private final JavaMailSender mailSender;
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    public void sendVerificationEmail(String destinationEmail, String tokenId) {
        String verificationUrl = String.format("http://localhost:8080/api/auth/verify?token=%s", tokenId);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinationEmail);
        message.setFrom("noreply@university.edu");
        message.setSubject("Activation de votre compte");
        message.setText("Bienvenue ! Veuillez activer votre compte en cliquant sur le lien suivant :\n" + verificationUrl);
        mailSender.send(message);
    }
}