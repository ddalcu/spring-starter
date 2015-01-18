package app.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
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
    
    private UserRepository repo;

    @Autowired
    public UserService(UserRepository repo) {
        this.repo = repo;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByName(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        if(requireActivation && !user.getActivation().equals("1")) {
            Application.log.debug("User [" + username + "] tried to login but is not activated");
            throw new UsernameNotFoundException(username);
        }
        List<GrantedAuthority> auth = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER");
        if (username.equals("admin")) {
            auth = AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_ADMIN");
        }
        String password = user.getPassword();

        return new org.springframework.security.core.userdetails.User(username, password, auth);
    }

    public Boolean register(User user) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (this.repo.findByName(user.getName()) == null) {
            Md5PasswordEncoder encoder = new Md5PasswordEncoder();
            String activation = encoder.encodePassword(user.getName(), "Application.secret");
            Application.log.debug("Setting user activation to-" + activation);
            user.setActivation(activation);
            this.repo.save(user);
            return true;
        }

        return false;
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
}