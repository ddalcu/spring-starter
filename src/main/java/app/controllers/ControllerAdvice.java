package app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import app.models.entity.User;
import app.services.UserService;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {

    @Autowired
    private UserService userService;

    @ModelAttribute("g_user")
    public User getCurrentUser() {

        return userService.getLoggedInUser();
    }
}
