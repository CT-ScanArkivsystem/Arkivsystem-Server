package no.ntnu.ctscanarkivsystemserver.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author TrymV
 * @source https://www.youtube.com/watch?v=TNt3GHuayXs&t=196s
 */
public class MyUserDetails implements UserDetails {

    private final User user;

    public MyUserDetails(User user) {
        this.user = user;
    }

    /**
     * Returns a list of the users granted authorities.
     * @return a list of the users granted authorities.
     * @source https://www.youtube.com/watch?v=i21h6ThUiWc&t=994s
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<Role> roles = user.getRoles();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for(Role role:roles) {
            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
        }
        return authorities;
    }

    /**
     * Returns the users password.
     * @return the users password.
     */
    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    /**
     * Returns the users username which in this case is the user email.
     * @return the users username which is the user email.
     */
    @Override
    public String getUsername() {
        return this.user.getEmail();
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
