package no.ntnu.ctscanarkivsystemserver.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * @author TrymV
 * @source https://www.youtube.com/watch?v=TNt3GHuayXs&t=196s
 */
public class MyUserDetails implements UserDetails {

    private final String userEmail;
    private final String userPassword;

    public MyUserDetails(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    /**
     * Returns the users password.
     * @return the users password.
     */
    @Override
    public String getPassword() {
        return this.userPassword;
    }

    /**
     * Returns the users username which in this case is the user email.
     * @return the users username which is the user email.
     */
    @Override
    public String getUsername() {
        return this.userEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
