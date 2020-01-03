package app.services;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import app.configs.ApplicationConfig;
import app.models.entity.User;

@Service
public class MailService {

    @Autowired
    private ApplicationConfig config;

    @Autowired
    private MailSender mailSender;

    public static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    public void sendMail(final String to, final String subject, final String text) {

        try {
            if (!config.isEmailMock()) {
                final SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(to);
                email.setSubject(subject);
                email.setFrom(config.getEmailFrom());
                email.setText(text);
                mailSender.send(email);
            }
            LOGGER.info("SENT EMAIL: TO={}|SUBJECT:{}|TEXT:{}", to, subject, text);
        } catch (final Exception e) {
            LOGGER.error("Error sending email", e);
        }
    }

    public void sendResetPassword(final String to, final String token) {

        final String url = config.getUrl() + "/user/reset-password-change?token=" + token;
        final String subject = "Reset Password";
        final String text = "Please click the following link to reset your password: " + url;
        sendMail(to, subject, text);
    }

    public void sendNewRegistration(final String to, final String token) {

        final String url = config.getUrl() + "/user/activate?activation=" + token;
        final String subject = "Please activate your account";
        final String text = "Please click the following link to activate your account: " + url;
        sendMail(to, subject, text);
    }

    public void sendNewActivationRequest(final String to, final String token) {

        sendNewRegistration(to, token);
    }

    public void sendErrorEmail(final Exception exception, final HttpServletRequest req, final User user) {

        final String subject = "Application Error: " + req.getRequestURL();
        final String text = "An error occured in your application: " + exception + "\r\nFor User:  " + user.getEmail();
        sendMail(config.getEmailSupport(), subject, text);
    }
}
