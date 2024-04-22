package com.smart.service;


	import java.util.Properties;

	import javax.mail.Authenticator;
	import javax.mail.Message;
	import javax.mail.PasswordAuthentication;
	import javax.mail.Session;
	import javax.mail.Transport;
	import javax.mail.internet.InternetAddress;
	import javax.mail.internet.MimeMessage;

	import org.springframework.stereotype.Service;

	@Service
	public class EmailService {

	    public boolean sendEmail(String subject, String message, String to) {
	        boolean f = false;

	        String from = "horakashish00@gmail.com";
	        String password = "ivcx lskb ifie zdzn"; // Use environment variable or external configuration instead

	        // Variable for gmail
	        String host = "smtp.gmail.com";

	        // Get the system properties
	        Properties properties = System.getProperties();
	        System.out.println("PROPERTIES: " + properties);

	        // Setting important information to properties object
	        properties.put("mail.smtp.host", host);
	        properties.put("mail.smtp.port", "465");
	        properties.put("mail.smtp.ssl.enable", "true");
	        properties.put("mail.smtp.auth", "true");

	        // Step 1: Get the session object
	        Session session = Session.getInstance(properties, new Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication(from, password);
	            }
	        });

	        session.setDebug(true);

	        // Step 2: Create a MimeMessage object
	        MimeMessage m = new MimeMessage(session);
	        try {
	            // Set From: header field
	            m.setFrom(new InternetAddress(from));

	            // Add recipient to message
	            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

	            // Set subject of the email
	            m.setSubject(subject);

	            // Set text of the email
	          //  m.setText(message);
	            m.setContent(message, "text/html");

	            // Step 3: Send message using Transport class
	            Transport.send(m);
	            System.out.println("Sent success......");
	            f = true;

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return f;
	    }
	}



