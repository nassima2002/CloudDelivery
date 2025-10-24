package com.project.deliveryms.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

@ApplicationScoped
public class EmailService {

    // Configuration Gmail uniquement
    private static final String GMAIL_EMAIL_FROM = "";
    private static final String GMAIL_APP_PASSWORD = ""; // App Password généré

    /**
     * Envoi d'un email via Gmail
     */
    public void sendEmail(String recipientEmail, String subject, String messageBody) throws MessagingException {
        Session session = getGmailSession();
        Message message = new MimeMessage(session);

        message.setFrom(new InternetAddress(GMAIL_EMAIL_FROM));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);
        message.setText(messageBody);

        Transport.send(message);
    }

    /**
     * Création de la session Gmail
     */
    private Session getGmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(GMAIL_EMAIL_FROM, GMAIL_APP_PASSWORD);
            }
        });
    }
}
