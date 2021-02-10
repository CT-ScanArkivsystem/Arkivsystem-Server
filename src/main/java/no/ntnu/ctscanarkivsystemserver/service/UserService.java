package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.Exception.EmailExistsException;
import no.ntnu.ctscanarkivsystemserver.dao.UserDao;
import no.ntnu.ctscanarkivsystemserver.model.MyUserDetails;
import no.ntnu.ctscanarkivsystemserver.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author TrymV
 */
@Service
public class UserService implements UserDetailsService {

    private final UserDao userDao;

    @Autowired
    public UserService(@Qualifier("postgreSQL") UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Adds a new user into the database.
     * @param user to be added into the database.
     * @return user which was added into the database.
     * @throws EmailExistsException if a user with given email already exists in the database.
     */
    public User addUser(User user) throws EmailExistsException{
        if(userDao.doesEmailExist(user.getEmail())) {
            throw new EmailExistsException(user.getEmail());
        }
        User newUser = new User(user.getFirstName(), user.getLastName(), user.getEmail(), passwordEncoder.encode(user.getPassword()));
        return userDao.insertUser(newUser);
    }

    public List<User> getAllUsers() {
        return userDao.selectAllUsers();
    }

    /**
     * Tries to find a user from the database and return the user as a new UserDetails.
     * If user is not found this returns null.
     * @param userName email of the user to find.
     * @return a new UserDetails with userName and password like found User in database.
     * @throws UsernameNotFoundException if no user with userName is found in the database.
     */
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userDao.getUserByEmail(userName);

        if(user == null) {
            throw new UsernameNotFoundException("User with userName: " + userName + " not found!");
        }

        return new MyUserDetails(user.getEmail(), user.getPassword());
    }
}
