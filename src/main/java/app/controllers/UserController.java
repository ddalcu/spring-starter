package app.controllers;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import app.models.User;
import app.repositories.UserRepository;
import app.services.MailService;
import app.services.UserService;

@Controller
// @RequestMapping("/user/*")
public class UserController {
    private Logger log = LoggerFactory.getLogger(UserController.class);
    
    @Value("${user.require-activation}")
    private Boolean requireActivation;
    
    private final UserRepository userRepository;

    @Autowired
    protected AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;
    
    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping("/login")
    public String login(User user) {
        return "user/login";
    }

    @RequestMapping("/user/list")
    public ModelAndView list() {
        Iterable<User> users = this.userRepository.findAll();
        return new ModelAndView("user/list", "users", users);
    }

    @RequestMapping(value = "/user/register", method = RequestMethod.GET)
    public String register(User user) {
        return "user/register";
    }
    
    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    public String registerPost(@Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            return "user/register";
        }
        
        User registeredUser = userService.register(user);
        if (registeredUser != null) {
           mailService.sendNewRegistration(user.getEmail(), registeredUser.getActivation());
            if(!requireActivation) {
                userService.autoLogin(user.getUserName());
                return "redirect:/";
            }
            return "user/register-success";
        } else {
            log.error("User already exists: " + user.getUserName());
            result.rejectValue("email", "error.alreadyExists", "This username or email already exists, please try to reset password instead.");
            return "user/register";
        }
    }
    
    @RequestMapping(value = "/user/reset-password")
    public String resetPasswordEmail(User user) {
        return "user/reset-password";
    }
    
    @RequestMapping(value = "/user/reset-password", method = RequestMethod.POST)
    public ModelAndView resetPasswordEmailPost(User user, BindingResult result) {
        User u = userRepository.findByEmail(user.getEmail());
        if(u == null) {
            result.rejectValue("email", "error.doesntExist", "We could not find this email in our databse");
        } else {
            String resetToken = userService.createResetPasswordToken(u);
            mailService.sendResetPassword(user.getEmail(), resetToken);
        }
        return new ModelAndView("user/reset-password", "message", "check your email");
    }

    @RequestMapping(value = "/user/reset-password-change")
    public String resetPasswordChange(User user, BindingResult result, Model model) {
        User u = userRepository.findByActivation(user.getActivation());
        if(user.getActivation().equals("1") || u == null) {
            result.rejectValue("activation", "error.doesntExist", "We could not find this reset password request.");
        } else {
            model.addAttribute("userName", u.getUserName());
        }
        return "user/reset-password-change";
    }
    
    @RequestMapping(value = "/user/reset-password-change", method = RequestMethod.POST)
    public ModelAndView resetPasswordChangePost(User user, BindingResult result) {
        Boolean isChanged = userService.resetPassword(user);
        if(isChanged) {
            userService.autoLogin(user.getUserName());
            return new ModelAndView("redirect:/");
        } else {
            return new ModelAndView("user/reset-password-change", "error", "Password could not be changed");
        }
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
