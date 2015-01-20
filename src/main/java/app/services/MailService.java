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
    
    public static void sendResetPassword(String to, String link) {
        String subject = "Reset Password";
        String text = "Please click the following link to reset your password: " + link;
        sendMail(to, subject, text);
    }
}
