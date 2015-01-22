package app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Autowired 
    private JavaMailSender javaMailSender;
    
    public void sendMail(String to, String subject, String text) {
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setTo(to);
            m.setSubject(subject);
            m.setFrom(fromEmail);
            m.setText(text);
            javaMailSender.send(m);
            System.out.println("SENT EMAIL: TO=" + to + "|SUBJECT:" + subject + "|TEXT:" + text);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    public void sendResetPassword(String to, String activation) {
        String subject = "Reset Password";
        String text = "Please click the following link to reset your password: " + activation;
        sendMail(to, subject, text);
    }
    
    public void sendNewRegistration(String to, String activation) {
        String subject = "Welcome to our website";
        String text = "Please click the following link to verify your email address: " + activation;
        sendMail(to, subject, text);
    }
}
