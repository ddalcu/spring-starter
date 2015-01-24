package app.controllers;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import app.models.User;
import app.repositories.UserRepository;
import app.services.MailService;
import app.services.UserService;

@Controller
// @RequestMapping("/user/*")
public class UserController {
    private Logger log = LoggerFactory.getLogger(UserController.class);
    
    @Value("${app.user.verification}")
    private Boolean requireActivation;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    protected AuthenticationManager authenticationManager;
    
    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @RequestMapping("/login")
    public String login(User user) {
        return "user/login";
    }

    @RequestMapping("/user/list")
    public String list(ModelMap map) {
        Iterable<User> users = this.userRepository.findAll();
        map.addAttribute("users", users);
        return "user/list";
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
           mailService.sendNewRegistration(user.getEmail(), registeredUser.getToken());
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
    public String resetPasswordEmailPost(User user, BindingResult result) {
        User u = userRepository.findOneByEmail(user.getEmail());
        if(u == null) {
            result.rejectValue("email", "error.doesntExist", "We could not find this email in our databse");
            return "user/reset-password";
        } else {
            String resetToken = userService.createResetPasswordToken(u, true);
            mailService.sendResetPassword(user.getEmail(), resetToken);
        }
        return "user/reset-password-sent";
    }

    @RequestMapping(value = "/user/reset-password-change")
    public String resetPasswordChange(User user, BindingResult result, Model model) {
        User u = userRepository.findOneByToken(user.getToken());
        if(user.getToken().equals("1") || u == null) {
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
    
    @RequestMapping("/user/activation-send")
    public ModelAndView activationSend(User user) {
        return new ModelAndView("/user/activation-send");
    }
    
    @RequestMapping(value = "/user/activation-send", method = RequestMethod.POST)
    public ModelAndView activationSendPost(User user, BindingResult result) {
        User u = userService.resetActivation(user.getEmail());
        if(u != null) {
            mailService.sendNewActivationRequest(u.getEmail(), u.getToken());
            return new ModelAndView("/user/activation-sent");
        } else {
            result.rejectValue("email", "error.doesntExist", "We could not find this email in our databse");
            return new ModelAndView("/user/activation-send");
        }
    }
    
    @RequestMapping("/user/delete")
    public String delete(Long id) {
        userService.delete(id);
        return "redirect:/user/list";
    }
    
    @RequestMapping("/user/activate")
    public String activate(String activation) {
        User u = userService.activate(activation);
        if(u != null) {
            userService.autoLogin(u);
            return "redirect:/";
        }
        return "redirect:/error?message=Could not activate with this activation code, please contact support";
    }
    
    @RequestMapping("/user/autologin")
    public String autoLogin(User user) {
        userService.autoLogin(user.getUserName());
        return "redirect:/";
    }

    
    @RequestMapping("/user/edit/{id}")
    public String edit(@PathVariable("id") Long id, User user) {
        User u;
        User loggedInUser = userService.getLoggedInUser();
        if(id == 0) {
            id = loggedInUser.getId();
        }
        if(loggedInUser.getId() != id && !loggedInUser.isAdmin()) {
            return "user/premission-denied";
        } else if (loggedInUser.isAdmin()) {
            u = userRepository.findOne(id);
        } else {
            u = loggedInUser;
        }
        user.setId(u.getId());
        user.setUserName(u.getUserName());
        user.setAddress(u.getAddress());
        user.setCompanyName(u.getCompanyName());
        user.setEmail(u.getEmail());
        user.setFirstName(u.getFirstName());
        user.setLastName(u.getLastName());
        
        return "/user/edit";
    }
    
    @RequestMapping(value = "/user/edit", method = RequestMethod.POST)
    public String editPost(@Valid User user, BindingResult result) {
        if (result.hasFieldErrors("email")) {
            return "/user/edit";
        }
        
        if(userService.getLoggedInUser().isAdmin()) {
            userService.updateUser(user);
        } else {
            userService.updateUser(userService.getLoggedInUser().getUserName(), user);
        }
        
        return "redirect:/user/edit/" + user.getId() + "?updated";
    }
}
