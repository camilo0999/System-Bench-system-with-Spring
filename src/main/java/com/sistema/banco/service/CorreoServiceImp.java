package com.sistema.banco.service;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.sistema.banco.models.Cliente;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class CorreoServiceImp implements CorreoServie {

    @Value("${email.sender}")
    private String emailUser;

    private JavaMailSender mailSender;

    private ClienteService clienteService;

    @Override
    public void enviarCorreo(String destinatario, String sujeto, String mensaje) {

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(emailUser);
            mailMessage.setTo(destinatario);
            mailMessage.setSubject(sujeto);
            mailMessage.setText(mensaje);
            mailSender.send(mailMessage);
        } catch (Exception e) {
            // Mejor manejo de excepciones y logging
            System.err.println("Error al enviar el correo: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void enviarCorreoArchivo(String destinatario, String sujeto, String mensaje, File file) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,
                    StandardCharsets.UTF_8.name());
            mimeMessageHelper.setFrom(emailUser);
            mimeMessageHelper.setTo(destinatario);
            mimeMessageHelper.setSubject(sujeto);
            mimeMessageHelper.setText(mensaje);
            mimeMessageHelper.addAttachment(file.getName(), file);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Mejor manejo de excepciones y logging
            System.err.println("Error al enviar el correo con archivo: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void cambioContrasena(String destinatario) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            Cliente cliente = clienteService.buscarClienteUsername(destinatario);
            String cuenta = cliente.getDocumento();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(emailUser);
            helper.setTo(destinatario);
            helper.setSubject("RESTABLECER CONTRASEÑA DE BANK");

            String htmlMsg = "<p>Haz clic <a href=\"http://localhost:8080/bank/recuperar/" + cuenta
                    + "\">aquí</a> para hacer el proceso de restablecer tu contraseña.</p>";
            helper.setText(htmlMsg, true);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            // Mejor manejo de excepciones y logging
            System.err.println("Error al enviar el correo de cambio de contraseña: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
