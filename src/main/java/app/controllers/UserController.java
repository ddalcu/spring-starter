package app.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import app.configs.ApplicationConfig;
import app.models.dto.UserDto;
import app.models.entity.User;
import app.repositories.UserRepository;
import app.services.MailService;
import app.services.UserService;

@Controller
public class UserController {

    private final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private ApplicationConfig config;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    public static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/login")
    public String login(final UserDto userDto) {

        return "user/login";
    }

    @GetMapping("/user/list")
    public String list(final ModelMap map) {

        final Iterable<User> users = this.userRepository.findAll();
        map.addAttribute("users", users);
        return "user/list";
    }

    @GetMapping("/user/register")
    public String register(final UserDto userDto) {

        return "user/register";
    }

    @PostMapping(value = "/user/register")
    public String registerPost(@Valid final UserDto userDto, final BindingResult result) {

        if (result.hasErrors()) {
            return "user/register";
        }

        final User registeredUser = userService.register(userDto);
        if (registeredUser != null) {
            mailService.sendNewRegistration(userDto.getEmail(), registeredUser.getToken());
            if (!config.isUserVerification()) {
                userService.autoLogin(userDto.getUserName());
                return "redirect:/";
            }
            return "user/register-success";
        } else {
            log.error("User already exists: {}", userDto.getUserName());
            result.rejectValue("email", "error.alreadyExists", "This username or email already exists, please try to reset password instead.");
            return "user/register";
        }
    }

    @GetMapping("/user/reset-password")
    public String resetPasswordEmail(final UserDto userDto) {

        return "user/reset-password";
    }

    @PostMapping("/user/reset-password")
    public String resetPasswordEmailPost(final UserDto userDto, final BindingResult result) {

        final User u = userRepository.findOneByEmail(userDto.getEmail());
        if (u == null) {
            result.rejectValue("email", "error.doesntExist", "We could not find this email in our databse");
            return "user/reset-password";
        } else {
            final String resetToken = userService.createResetPasswordToken(u, true);
            LOGGER.debug("Resetting password for user: {}, new token: {}", userDto.getEmail(), resetToken);
            mailService.sendResetPassword(userDto.getEmail(), resetToken);
        }
        return "user/reset-password-sent";
    }

    @GetMapping("/user/reset-password-change")
    public String resetPasswordChange(final UserDto userDto, final BindingResult result, final Model model) {

        final User u = userRepository.findOneByToken(userDto.getToken());
        if (userDto.getToken().equals("1") || u == null) {
            result.rejectValue("activation", "error.doesntExist", "We could not find this reset password request.");
        } else {
            model.addAttribute("userName", u.getUserName());
        }
        return "user/reset-password-change";
    }

    @PostMapping("/user/reset-password-change")
    public String resetPasswordChangePost(final UserDto userDto, final BindingResult result, final Model model) {

        final boolean isChanged = userService.resetPassword(userDto);
        if (isChanged) {
            userService.autoLogin(userDto.getUserName());
            return "redirect:/";
        } else {
            model.addAttribute("error", "Password could not be changed");
            return "user/reset-password-change";
        }
    }

    @GetMapping("/user/activation-send")
    public String activationSend(final UserDto userDto) {

        return "/user/activation-send";
    }

    @PostMapping("/user/activation-send")
    public String activationSendPost(final UserDto userDto, final BindingResult result) {

        final User u = userService.resetActivation(userDto.getEmail());
        if (u != null) {
            mailService.sendNewActivationRequest(u.getEmail(), u.getToken());
            return "/user/activation-sent";
        } else {
            result.rejectValue("email", "error.doesntExist", "We could not find this email in our databse");
            return "/user/activation-send";
        }
    }

    @GetMapping("/user/delete")
    public String delete(final Long id) {

        userService.delete(id);
        return "redirect:/user/list";
    }

    @GetMapping("/user/activate")
    public String activate(final String activation) {

        final User u = userService.activate(activation);
        if (u != null) {
            userService.autoLogin(u);
            return "redirect:/";
        }
        return "redirect:/error?message=Could not activate with this activation code, please contact support";
    }

    @GetMapping("/user/autologin")
    public String autoLogin(final UserDto userDto) {

        userService.autoLogin(userDto.getUserName());
        return "redirect:/";
    }

    @GetMapping("/user/edit/{id}")
    public String edit(@PathVariable("id") final Long id, final UserDto userDto) {

        final User u;
        Long lookupId = id;
        final User loggedInUser = userService.getLoggedInUser();
        if (id == 0) {
            lookupId = loggedInUser.getId();
        }
        if (!loggedInUser.getId().equals(lookupId) && !loggedInUser.isAdmin()) {
            return "user/premission-denied";
        } else if (loggedInUser.isAdmin()) {
            u = userRepository.findById(lookupId).orElse(null);
        } else {
            u = loggedInUser;
        }
        userDto.setId(u.getId());
        userDto.setUserName(u.getUserName());
        userDto.setAddress(u.getAddress());
        userDto.setCompanyName(u.getCompanyName());
        userDto.setEmail(u.getEmail());
        userDto.setFirstName(u.getFirstName());
        userDto.setLastName(u.getLastName());

        return "/user/edit";
    }

    @PostMapping("/user/edit")
    public String edit(@Valid final UserDto userDto, final BindingResult result) {

        if (result.hasFieldErrors("email")) {
            return "/user/edit";
        }
        if (userService.getLoggedInUser().isAdmin()) {
            userService.updateUser(userDto);
        } else {
            userService.updateUser(userService.getLoggedInUser().getUserName(), userDto);
        }

        if (userService.getLoggedInUser().getId().equals(userDto.getId())) {
            // put updated user to session
            userService.getLoggedInUser(true);
        }

        return "redirect:/user/edit/" + userDto.getId() + "?updated";
    }

    @PostMapping("/user/upload")
    public String handleFileUpload(@RequestParam("file") final MultipartFile file) {

        final Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        final String fileName = formatter.format(Calendar.getInstance().getTime()) + "_thumbnail.jpg";
        final User user = userService.getLoggedInUser();
        if (!file.isEmpty()) {
            try {
                final String saveDirectory = config.getUserRoot() + File.separator + user.getId() + File.separator;
                createSaveDirectory(saveDirectory);

                final byte[] bytes = file.getBytes();

                final ByteArrayInputStream imageInputStream = new ByteArrayInputStream(bytes);
                final BufferedImage image = ImageIO.read(imageInputStream);
                final BufferedImage thumbnail = Scalr.resize(image, 200);

                final File thumbnailOut = new File(saveDirectory + fileName);
                ImageIO.write(thumbnail, "png", thumbnailOut);

                userService.updateProfilePicture(user, fileName);
                userService.getLoggedInUser(true);
                log.debug("Image Saved::: {}", fileName);
            } catch (final Exception e) {
                log.error("Error Uploading File", e);
            }
        }
        return "redirect:/user/edit/" + user.getId();
    }

    private void createSaveDirectory(final String saveDirectory) {

        final File test = new File(saveDirectory);
        if (!test.exists()) {
            try {
                test.mkdirs();
            } catch (final Exception e) {
                LOGGER.error("Error creating user directory", e);
            }
        }
    }

    @GetMapping(value = "/user/profile-picture", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] profilePicture() throws IOException {

        final User u = userService.getLoggedInUser();
        final String profilePicture = config.getUserRoot() + File.separator + u.getId() + File.separator + u.getProfilePicture();
        if (new File(profilePicture).exists()) {
            return IOUtils.toByteArray(new FileInputStream(profilePicture));
        } else {
            return null;
        }
    }
}
