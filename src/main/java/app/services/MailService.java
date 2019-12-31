package app.services;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import app.models.User;

@Service
public class MailService {

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

    @Value("${app.email.support}")
    private String supportEmail;

    @Autowired
    private MailSender mailSender;

    public void sendMail(final String to, final String subject, final String text) {

        try {
            final SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(to);
            email.setSubject(subject);
            email.setFrom(fromEmail);
            email.setText(text);
            mailSender.send(email);
            System.out.println("SENT EMAIL: TO=" + to + "|SUBJECT:" + subject + "|TEXT:" + text);
        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void sendResetPassword(final String to, final String token) {

        final String url = appUrl + "/user/reset-password-change?token=" + token;
        final String subject = "Reset Password";
        final String text = "Please click the following link to reset your password: " + url;
        sendMail(to, subject, text);
    }

    public void sendNewRegistration(final String to, final String token) {

        final String url = appUrl + "/user/activate?activation=" + token;
        final String subject = "Please activate your account";
        final String text = "Please click the following link to activate your account: " + url;
        sendMail(to, subject, text);
    }

    public void sendNewActivationRequest(final String to, final String token) {

        sendNewRegistration(to, token);
    }

    public void sendErrorEmail(final Exception e, final HttpServletRequest req, final User user) {

        final String subject = "Application Error: " + req.getRequestURL();
        final String text = "An error occured in your application: " + e + "\r\nFor User:  " + user.getEmail();
        sendMail(supportEmail, subject, text);
    }
}
