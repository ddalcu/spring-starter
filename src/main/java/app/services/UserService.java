package app.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
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
    @Value("${user.require-activation}")
    private Boolean requireActivation;
    
    @Value("${app.secret}")
    private String applicationSecret;
    
    private UserRepository repo;
    
    @Autowired
    public UserService(UserRepository repo) {
        this.repo = repo;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByUserName(username);
        
        if(user == null) {
            user = repo.findByEmail(username);
            if(user == null) {
                throw new UsernameNotFoundException(username);
            }
        }
        if(requireActivation && !user.getActivation().equals("1")) {
            Application.log.debug("User [" + username + "] tried to login but is not activated");
            throw new UsernameNotFoundException(username);
        }
        
        List<GrantedAuthority> auth = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRole());

        String password = user.getPassword();

        return new org.springframework.security.core.userdetails.User(username, password, auth);
    }
    
    public void autoLogin(String username) {
        UserDetails userDetails = this.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken (userDetails, null, userDetails.getAuthorities());
        
        SecurityContextHolder.getContext().setAuthentication(auth);
        if(auth.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    public User register(User user) {
        user.setPassword(encodeUserPassword(user.getPassword()));

        if (this.repo.findByUserName(user.getUserName()) == null && this.repo.findByEmail(user.getEmail()) == null) {
            Md5PasswordEncoder encoder = new Md5PasswordEncoder();
            String activation = encoder.encodePassword(user.getUserName(), applicationSecret);
            user.setActivation(activation);
            this.repo.save(user);
            return user;
        }

        return null;
    }
    
    public String encodeUserPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }
    
    public Boolean delete(Long id) {
        this.repo.delete(id);
        return true;
    }
    
    public Boolean activate(String activation) {
        if(activation.equals("1") || activation.length()<5) {
            return false;
        }
        User u = this.repo.findByActivation(activation);
        if(u!=null) {
            u.setActivation("1");
            this.repo.save(u);
            return true;
        }
        return false;
    }
    
    public String createResetPasswordToken(User user) {
        Md5PasswordEncoder encoder = new Md5PasswordEncoder();
        String resetToken = encoder.encodePassword(user.getEmail(), applicationSecret);
        
        user.setActivation(resetToken);
        this.repo.save(user);
        return resetToken;
    }
    
    public Boolean resetPassword(User user) {
        User u = this.repo.findByUserName(user.getUserName());
        if(u != null) {
            u.setPassword(encodeUserPassword(user.getPassword()));
            u.setActivation("1");
            this.repo.save(u);
            return true;
        }
        return false;
    }
}