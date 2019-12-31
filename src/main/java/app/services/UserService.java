package app.services;

import java.util.List;
import javax.servlet.http.HttpSession;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import app.Application;
import app.models.User;
import app.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Value("${app.user.verification}")
    private boolean requireActivation;

    @Value("${app.secret}")
    private String applicationSecret;

    @Autowired
    private UserRepository repo;

    @Autowired
    private HttpSession httpSession;

    private static final String CURRENT_USER_KEY = "CURRENT_USER";

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {

        final User user = repo.findOneByUserNameOrEmail(username, username);

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        if (requireActivation && !user.getToken().equals("1")) {
            Application.log.error("User [{}] tried to login but is not activated", username);
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

    public User register(final User user) {

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

        if (activation.equals("1") || activation.length() < 5) {
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

        final String toEncode = user.getUserName() + applicationSecret;
        final String activationToken = MD5Encoder.encode(toEncode.getBytes());
        if (save) {
            user.setToken(activationToken);
            this.repo.save(user);
        }
        return activationToken;
    }

    public String createResetPasswordToken(final User user, final boolean save) {

        final String toEncode = user.getEmail() + applicationSecret;
        final String resetToken = MD5Encoder.encode(toEncode.getBytes());
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

    public Boolean resetPassword(final User user) {

        final User u = this.repo.findOneByUserName(user.getUserName());
        if (u != null) {
            u.setPassword(encodeUserPassword(user.getPassword()));
            u.setToken("1");
            this.repo.save(u);
            return true;
        }
        return false;
    }

    public void updateUser(final User user) {

        updateUser(user.getUserName(), user);
    }

    public void updateUser(final String userName, final User newData) {

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