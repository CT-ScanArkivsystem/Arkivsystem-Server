package no.ntnu.ctscanarkivsystemserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import sun.security.util.Password;

/**
 * @author TrymV
 * @source https://www.youtube.com/watch?v=iyXne7dIn7U&list=PLqq-6Pq4lTTYTEooakHchTGglSvkZAjnE&index=4
 * @source https://www.youtube.com/watch?v=TNt3GHuayXs&t=196s
 */
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserDetailsService userService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    //@Bean
    //public BCryptPasswordEncoder getPasswordEncoder() {
    //    return new BCryptPasswordEncoder();
    //}

    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * Set up the role required to visit certain paths.
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //TODO This line will allow POST requests without being logged in.
        http.cors().and().csrf().disable();
        //TODO Fix this when developing the role system.
        http.authorizeRequests()
                .antMatchers("/admin").hasRole("ADMIN")
                .antMatchers("/professor").hasAnyRole("ADMIN", "PROFESSOR")
                .antMatchers("/user").hasAnyRole("ADMIN", "PROFESSOR", "user")
                .antMatchers("/api").permitAll()
                .and().formLogin();
    }
}
