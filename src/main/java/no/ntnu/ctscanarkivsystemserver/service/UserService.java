package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.exception.EmailExistsException;
import no.ntnu.ctscanarkivsystemserver.dao.UserDao;
import no.ntnu.ctscanarkivsystemserver.exception.UserNotFoundException;
import no.ntnu.ctscanarkivsystemserver.model.MyUserDetails;
import no.ntnu.ctscanarkivsystemserver.model.Role;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.model.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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
     * @throws IllegalArgumentException if email is null in UserDTO object.
     */
    public User addUser(UserDTO user) throws EmailExistsException, IllegalArgumentException {
        if(userDao.doesEmailExist(user.getEmail())) {
            throw new EmailExistsException(user.getEmail());
        }
        User newUser = new User(user.getFirstName(), user.getLastName(), user.getEmail(), passwordEncoder.encode(user.getPassword()));
        return userDao.insertUser(newUser, user.getRole());
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

        return new MyUserDetails(user);
    }

    /**
     * Return the current logged in user.
     * @return current logged in user.
     * @throws UserNotFoundException if user is not logged in.
     */
    public User getCurrentLoggedUser() throws UserNotFoundException{
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return getUserByEmail(userDetails.getUsername());
    }

    /**
     * Gets a user by email.
     * @param email of user to get.
     * @return user with email in database.
     * @throws UserNotFoundException if no user was found.
     */
    public User getUserByEmail(String email) throws UserNotFoundException {
        User user = userDao.getUserByEmail(email);

        if(user == null) {
            throw new UserNotFoundException(email);
        }
        return user;
    }

    /**
     * Gets a user by id.
     * @param id of user to get.
     * @return user with id in database.
     * @throws UserNotFoundException if no user was found.
     */
    private User getUserById(UUID id) throws UserNotFoundException {
        User user = userDao.getUserById(id);

        if(user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }

    /**
     * Find and change a user.
     * @param changes user object with the changes to be done to the user.
     *                Id or email will be used to find the user.
     * @return changed user.
     * @throws UserNotFoundException if no user was found with id or email.
     * @throws EmailExistsException if you try to change email to one which already exist.
     * @throws IllegalArgumentException if email in UserDTO object is null.
     */
    public User editUser(UserDTO changes) throws UserNotFoundException, EmailExistsException, IllegalArgumentException{
        User user;
        if(!changes.getUserId().toString().isEmpty()) {
            user = getUserById(changes.getUserId());
            if(!user.getEmail().equals(changes.getEmail()) && userDao.doesEmailExist(changes.getEmail())) {
                throw new EmailExistsException(changes.getEmail());
            }
        } else {
            user = getUserByEmail(changes.getEmail());
        }
        if(changes.getPassword() != null && !changes.getPassword().trim().isEmpty()) {
            changes.setPassword(passwordEncoder.encode(changes.getPassword()));
        }
        return userDao.editUser(user, changes);
    }

    /**
     * Removes a user from the system.
     * @param userId id of user to be removed.
     * @return true if user was successfully removed.
     * @throws UserNotFoundException if no user was found with id.
     */
    public boolean removeUser(UUID userId) throws UserNotFoundException{
        User userToBeRemoved = getUserById(userId);
        return userDao.removeUser(userToBeRemoved);
    }

    /**
     * Checks if parameter is a valid role.
     * @param role to be check if is valid.
     * @return true if parameter is a valid role.
     */
    public boolean isRoleValid(String role) {
        return role.equals(Role.ADMIN) || role.equals(Role.ACADEMIC) || role.equals(Role.USER);
    }
}
