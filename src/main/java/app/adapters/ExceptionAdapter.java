package app.adapters;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import app.configs.ApplicationConfig;
import app.models.entity.User;
import app.services.MailService;
import app.services.UserService;

@ControllerAdvice
class ExceptionAdapter {

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @Autowired
    private ApplicationConfig config;

    public static final String DEFAULT_ERROR_VIEW = "error";

    public static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAdapter.class);

    @ExceptionHandler(value = Exception.class)
    public String defaultErrorHandler(final HttpServletRequest req, final Exception exception, final Model model) throws Exception {

        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it - like the OrderNotFoundException example
        // at the start of this post.
        // AnnotationUtils is a Spring Framework utility class.
        if (AnnotationUtils.findAnnotation(exception.getClass(), ResponseStatus.class) != null) {
            throw exception;
        }

        // Otherwise setup and send the user to a default error-view.
        final User user = userService.getLoggedInUser();

        model.addAttribute("exception", exception);
        model.addAttribute("url", req.getRequestURL());
        model.addAttribute("user", user);
        model.addAttribute("support", config.getEmailSupport());

        if (config.isEmailErrors()) {
            mailService.sendErrorEmail(exception, req, user);
        }
        LOGGER.error("Error handler invoked", exception);
        return DEFAULT_ERROR_VIEW;
    }
}