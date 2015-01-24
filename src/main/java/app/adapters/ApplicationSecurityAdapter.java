package app.adapters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import app.services.UserService;

@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class ApplicationSecurityAdapter extends WebSecurityConfigurerAdapter {
    @Autowired
    private SecurityProperties security;

    @Autowired
    private UserService userService;
    
    @Value("${app.secret}")
    private String applicationSecret;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
        .antMatchers("/user/register").permitAll()
        .antMatchers("/user/activate").permitAll()
        .antMatchers("/user/activation-send").permitAll()
        .antMatchers("/user/reset-password").permitAll()
        .antMatchers("/user/reset-password-change").permitAll()
        .antMatchers("/user/autologin").access("hasRole('ROLE_ADMIN')")
        .antMatchers("/user/delete").access("hasRole('ROLE_ADMIN')")
        .antMatchers("/img/**").permitAll()
        .antMatchers("/images/**").permitAll()
        .antMatchers("/fonts/**").permitAll()
        .anyRequest().authenticated()
        .and()
            .formLogin().loginPage("/login").failureUrl("/login?error").permitAll()
        .and()
            .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login")
        .and()
            .rememberMe().key(applicationSecret)
            .tokenValiditySeconds(31536000);
    }
    
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder());
    }
}
