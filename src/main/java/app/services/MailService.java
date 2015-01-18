package app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class MailService {
    @Autowired 
    private static JavaMailSender javaMailSender;
    
    public static void sendMail(String to, String subject, String text) {
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setTo(to);
            m.setSubject(subject);
            //m.setFrom("");
            m.setText(text);
            javaMailSender.send(m);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
