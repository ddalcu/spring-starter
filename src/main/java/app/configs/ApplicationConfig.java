package app.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@ConfigurationPropertiesScan("application.properties")
public class ApplicationConfig {

    private String secret;

    private String url;

    private String emailFrom;

    private String emailSupport;

    private boolean emailErrors;

    private boolean emailMock;

    private String userRoot;

    private boolean userVerification;

    public String getSecret() {

        return secret;
    }

    public void setSecret(final String secret) {

        this.secret = secret;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(final String url) {

        this.url = url;
    }

    public String getEmailFrom() {

        return emailFrom;
    }

    public void setEmailFrom(final String emailFrom) {

        this.emailFrom = emailFrom;
    }

    public String getEmailSupport() {

        return emailSupport;
    }

    public void setEmailSupport(final String emailSupport) {

        this.emailSupport = emailSupport;
    }

    public boolean isEmailErrors() {

        return emailErrors;
    }

    public void setEmailErrors(final boolean emailErrors) {

        this.emailErrors = emailErrors;
    }

    public boolean isEmailMock() {

        return emailMock;
    }

    public void setEmailMock(final boolean emailMock) {

        this.emailMock = emailMock;
    }

    public String getUserRoot() {

        return userRoot;
    }

    public void setUserRoot(final String userRoot) {

        this.userRoot = userRoot;
    }

    public boolean isUserVerification() {

        return userVerification;
    }

    public void setUserVerification(final boolean userVerification) {

        this.userVerification = userVerification;
    }

}
