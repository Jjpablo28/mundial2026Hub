package co.edu.unbosque.mundialhub.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.mundialhub.exception.Exceptions;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Servicio encargado de la gestión y envío de correos electrónicos
 * dentro del sistema VeltrixPass.
 * * Centraliza la carga de plantillas y el envío de mensajes utilizando
 * {@link JavaMailSender}, aplicando validaciones de negocio previas.
 * * @author TuNombre / Grupo de Desarrollo
 * @version 1.0
 */
@Service
public class EmailService {



    /** Sender de Spring Mail para la infraestructura de correos. */
    @Autowired
    private JavaMailSender mailSender;
    
    /** Instancia del validador centralizado de excepciones de negocio. */
    private final Exceptions validator = new Exceptions();
    
    /**
     * Carga el contenido de una plantilla de correo electrónico desde los recursos del sistema.
     * Transformación interna de excepciones de E/S a excepciones de negocio.
     *
     * @param path Ruta absoluta o relativa dentro de los recursos (resources) donde se aloja la plantilla.
     * @return El contenido de la plantilla convertido a una cadena de texto en formato UTF-8.
     * @throws Exceptions.ResourceNotFoundException Si no se encuentra el archivo en la ruta especificada.
     * @throws Exceptions.ValidationException       Si ocurre un error de lectura de entrada/salida (IOException).
     */
    private String loadTemplate(String path) {
        try {
            var inputStream = getClass().getResourceAsStream(path);
            if (inputStream == null) {
                throw new Exceptions.ResourceNotFoundException("No se encontró la plantilla de correo en la ruta: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new Exceptions.ValidationException("Error al leer la plantilla de correo: " + e.getMessage());
        }
    }
    
    /**
     * Envía un correo electrónico estándar utilizando formato HTML.
     * Valida la estructura del correo de destino antes de proceder con el envío técnico.
     *
     * @param destination Dirección de correo electrónico del destinatario (Debe cumplir formato Gmail válido).
     * @param subject     Asunto del correo electrónico.
     * @param body        Cuerpo del mensaje (soporta etiquetas HTML).
     * @throws Exceptions.ValidationException Si la dirección de correo no tiene un formato válido
     * o si ocurre un fallo técnico en la infraestructura de mensajería (MessagingException).
     */
    public void sendEmailDefault(String destination, String subject, String body) {
        
        // 1. Validación de negocio: Formato del correo electrónico
        if (!validator.isGmailValid(destination)) {
            throw new Exceptions.ValidationException("El correo de destino '" + destination + "' no es un correo Gmail válido.");
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(destination);
            helper.setSubject(subject);
            helper.setFrom("info.mundial2026@gmail.com"); 
            helper.setText(body, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            // Se envuelve la excepción comprobada en una RuntimeException de negocio
            throw new Exceptions.ValidationException("Error al estructurar o enviar el correo electrónico: " + e.getMessage());
        }
    }
}
