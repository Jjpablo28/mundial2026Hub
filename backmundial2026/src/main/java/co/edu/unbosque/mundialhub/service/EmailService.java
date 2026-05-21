package co.edu.unbosque.mundialhub.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
	
	
	private String loadTemplate(String path) throws IOException {
	    return new String(
	        getClass().getResourceAsStream(path).readAllBytes(),
	        StandardCharsets.UTF_8
	    );
	}
	
	
	public void sendEmailDefault(String destination, String subject, String body) throws MessagingException{
		
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
		
		helper.setTo(destination);
		helper.setSubject(subject);
		helper.setFrom("info.mundial2026@gmail.com"); 
		helper.setText(body, true);
		
		mailSender.send(message);
	}
}
