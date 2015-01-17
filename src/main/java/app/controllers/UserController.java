package app.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.models.User;
import app.repositories.UserRepository;

@Controller
//@RequestMapping("/user/*")
public class UserController {
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @RequestMapping("/login")
    public String login() {
        return "user/login";
    }
    
    @RequestMapping("/user/list")
    public ModelAndView list() {
        Iterable<User> users = this.userRepository.findAll();
        return new ModelAndView("user/list", "users", users);
    }
    
    @RequestMapping(value = "/user/register", method = RequestMethod.GET)
    public String register() {
        return "user/register";
    }
    
    /*This method should be customized by you, do not use it in its current form, needs much improvement */
    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public @ResponseBody User register(@Valid User user, BindingResult result,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            //do stuff here
            //return new ModelAndView("user/register", "formErrors", result.getAllErrors());
        }


        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword( passwordEncoder.encode(user.getPassword()) );
        
        //todo..make sure user does not exist
        this.userRepository.save(user);
        
        return user;
    }
}
