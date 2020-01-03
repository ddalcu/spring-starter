package app.services;

import java.util.List;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.Charsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import app.Application;
import app.configs.ApplicationConfig;
import app.models.dto.UserDto;
import app.models.entity.User;
import app.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private static final int INVALID_ACTIVATION_LENGTH = 5;

    @Autowired
    private ApplicationConfig config;

    @Autowired
    private UserRepository repo;

    @Autowired
    private HttpSession httpSession;

    private static final String CURRENT_USER_KEY = "CURRENT_USER";

    @Override
    public UserDetails loadUserByUsername(final String username) {

        final User user = repo.findOneByUserNameOrEmail(username, username);

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        if (config.isUserVerification() && !user.getToken().equals("1")) {
            Application.LOGGER.error("User [{}] tried to login but is not activated", username);
            throw new UsernameNotFoundException(username + " has not been activated yet");
        }
        httpSession.setAttribute(CURRENT_USER_KEY, user);
        final List<GrantedAuthority> auth = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRole());

        return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(), auth);
    }

    public void autoLogin(final User user) {

        autoLogin(user.getUserName());
    }

    public void autoLogin(final String username) {

        final UserDetails userDetails = this.loadUserByUsername(username);
        final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
        if (auth.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    public User register(final UserDto userDto) {

        final User user = userDto.toEntity();
        user.setPassword(encodeUserPassword(user.getPassword()));

        if (this.repo.findOneByUserName(user.getUserName()) == null && this.repo.findOneByEmail(user.getEmail()) == null) {
            final String activation = createActivationToken(user, false);
            user.setToken(activation);
            this.repo.save(user);
            return user;
        }

        return null;
    }

    public String encodeUserPassword(final String password) {

        final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    public Boolean delete(final Long id) {

        this.repo.deleteById(id);
        return true;
    }

    public User activate(final String activation) {

        if ("1".equals(activation) || activation.length() < INVALID_ACTIVATION_LENGTH) {
            return null;
        }
        final User u = this.repo.findOneByToken(activation);
        if (u != null) {
            u.setToken("1");
            this.repo.save(u);
            return u;
        }
        return null;
    }

    public String createActivationToken(final User user, final boolean save) {

        final String toEncode = user.getUserName() + config.getSecret();
        final String activationToken = DigestUtils.md5DigestAsHex(toEncode.getBytes(Charsets.UTF_8));
        if (save) {
            user.setToken(activationToken);
            this.repo.save(user);
        }
        return activationToken;
    }

    public String createResetPasswordToken(final User user, final boolean save) {

        final String toEncode = user.getEmail() + config.getSecret();
        final String resetToken = DigestUtils.md5DigestAsHex(toEncode.getBytes(Charsets.UTF_8));
        if (save) {
            user.setToken(resetToken);
            this.repo.save(user);
        }
        return resetToken;
    }

    public User resetActivation(final String email) {

        final User u = this.repo.findOneByEmail(email);
        if (u != null) {
            createActivationToken(u, true);
            return u;
        }
        return null;
    }

    public Boolean resetPassword(final UserDto userDto) {

        final User u = this.repo.findOneByUserName(userDto.getUserName());
        if (u != null) {
            u.setPassword(encodeUserPassword(userDto.getPassword()));
            u.setToken("1");
            this.repo.save(u);
            return true;
        }
        return false;
    }

    public void updateUser(final UserDto user) {

        updateUser(user.getUserName(), user);
    }

    public void updateUser(final String userName, final UserDto newData) {

        this.repo.updateUser(
                userName,
                newData.getEmail(),
                newData.getFirstName(),
                newData.getLastName(),
                newData.getAddress(),
                newData.getCompanyName());
    }

    public User getLoggedInUser() {

        return getLoggedInUser(false);
    }

    public User getLoggedInUser(final boolean forceFresh) {

        final String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = (User) httpSession.getAttribute(CURRENT_USER_KEY);
        if (forceFresh || httpSession.getAttribute(CURRENT_USER_KEY) == null) {
            user = this.repo.findOneByUserName(userName);
            httpSession.setAttribute(CURRENT_USER_KEY, user);
        }
        return user;
    }

    public void updateLastLogin(final String userName) {

        this.repo.updateLastLogin(userName);
    }

    public void updateProfilePicture(final User user, final String profilePicture) {

        this.repo.updateProfilePicture(user.getUserName(), profilePicture);
    }
}