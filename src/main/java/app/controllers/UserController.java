package app.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import app.models.User;
import app.repositories.UserRepository;
import app.services.MailService;
import app.services.UserService;

@Controller
// @RequestMapping("/user/*")
public class UserController {
    @Value("${user.require-activation}")
    private Boolean requireActivation;
    
    private final UserRepository userRepository;

    @Autowired
    protected AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;
    
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Logged in::" + authentication.getName());
        Iterable<User> users = this.userRepository.findAll();
        return new ModelAndView("user/list", "users", users);
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.GET)
    public String register() {
        return "user/register";
    }

    /*
     * This method should be customized by you, do not use it in its current
     * form, needs much improvement
     */
    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public ModelAndView register(@Valid User user, BindingResult result, RedirectAttributes redirect, HttpServletRequest request) {
        if (result.hasErrors()) {
            return new ModelAndView("user/register", "formErrors", result.getAllErrors());
        }
        
        if (userService.register(user)) {
            MailService.sendMail(user.getEmail(), "Test", "You have registered");
            if(!requireActivation) {
                userService.autoLogin(user.getUserName());
                return new ModelAndView("redirect:/");
            }
            return new ModelAndView("user/register-success");
        } else {
            return new ModelAndView("user/register", "formErrors", "User already exists");
        }
    }
    
    @RequestMapping("/user/mail")
    public @ResponseBody Boolean mail() {
        MailService.sendMail("master@asus", "Test from spring", "This is a message");
        return true;
    }
    
    @RequestMapping("/user/delete")
    public @ResponseBody Boolean delete(Long id) {
        userService.delete(id);
        return true;
    }
    
    @RequestMapping("/user/get")
    public @ResponseBody Iterable<User> get() {
        return userRepository.findAll();
    }
    
    @RequestMapping("/user/activate")
    public @ResponseBody Boolean activate(String activation) {
        return userService.activate(activation);
    }
    
    @RequestMapping("/user/autologin")
    public String autoLogin(User user) {
        userService.autoLogin(user.getUserName());
        return "redirect:/";
    }
}
