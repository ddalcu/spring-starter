package app.adapters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import app.services.UserService;

@Component
public class LoginAdapter implements ApplicationListener<InteractiveAuthenticationSuccessEvent> {
    @Autowired
    private UserService userService;
    
    @Override
    public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
        userService.updateLastLogin(event.getAuthentication().getName());
    }

}
