package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.Role;
import no.ntnu.ctscanarkivsystemserver.model.User;
import no.ntnu.ctscanarkivsystemserver.model.UserDTO;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author TrymV
 */
@Repository("postgreSQL")
public class UserDataAccessService implements UserDao{

    @PersistenceContext
    EntityManager em;

    /**
     * Uses the EntityManager to insert a new user into the database.
     * @param user to be added to the database.
     * @return the added user.
     */
    @Transactional
    @Override
    public User insertUser(User user, String role) {
        Role userRole = em.find(Role.class, "ROLE_" + role);
        user.getRoles().add(userRole);
        em.persist(user);
        em.flush();
        System.out.println("New user id: " + user.getUserId());
        return user;
    }

    /**
     * Uses the EntityManager to retrieve all the users from the database and return them as a List.
     * @return all users in the database as a List.
     */
    @Override
    public List<User> selectAllUsers() {
        Query query = em.createNamedQuery(User.FIND_ALL_USERS);
        return query.getResultList();
    }

    /**
     * Return true if user with email is found in database.
     * @param email to see if already exists in database.
     * @return true if user with email already exist in database.
     */
    @Override
    public boolean doesEmailExist(String email) {
        Query query = em.createNamedQuery(User.FIND_USER_BY_EMAIL);
        email = email.toLowerCase();
        query.setParameter("email", email);
        List<User> queryResult = query.getResultList();
        return !queryResult.isEmpty();
    }

    /**
     * Changes an existing user in the database.
     * @param userToBeChanged user in the database to be changed.
     * @param changes a user object with the changes. If a variable is empty there will
     *                be no changes on that variable.
     * @return the changed user.
     */
    @Transactional
    @Override
    public User editUser(User userToBeChanged, UserDTO changes) {
        em.refresh(userToBeChanged);
        prepareUserForEdit(userToBeChanged);
        if(!changes.getRole().isEmpty()) {
            Role userRole = em.find(Role.class, "ROLE_" + changes.getRole());
            if (!userToBeChanged.getRoles().get(0).getRoleName().equals(userRole.getRoleName())) {
                userToBeChanged.getRoles().remove(0);
                userToBeChanged.getRoles().add(userRole);
            }
        }
        if(!changes.getEmail().trim().isEmpty()) {
            userToBeChanged.setEmail(changes.getEmail().trim().toLowerCase());
        }
        if(!changes.getFirstName().trim().isEmpty()) {
            userToBeChanged.setFirstName(changes.getFirstName().trim());
        }
        if(!changes.getLastName().trim().isEmpty()) {
            userToBeChanged.setLastName(changes.getLastName().trim());
        }
        if(!changes.getPassword().trim().isEmpty()) {
            userToBeChanged.setPassword(changes.getPassword().trim());
        }

        return saveUser(userToBeChanged);
    }

    /**
     * Prepares the database for change.
     * @param user to be changed in the database.
     */
    private void prepareUserForEdit(User user) {
        System.out.println("user getting ready for edit.");
        if(user != null) {
            try {
                em.lock(user, LockModeType.PESSIMISTIC_WRITE);
            } catch (Exception e) {
                System.out.println("Exception in prepare user for edit: " + e.getMessage());
            }
        }
    }

    /**
     * Merge a user into the database and then lock it.
     * @param userToSave group to be merged.
     * @return user if merge was successful else null.
     */
    private User saveUser(User userToSave) {
        System.out.println("Trying to save user.");
        if(userToSave != null) {
            try {
                em.merge(userToSave);
                em.lock(userToSave, LockModeType.NONE);
                em.flush();
                return userToSave;
            } catch (Exception e) {
                System.out.println("Exception in save user: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Search the database for a user with the id. If found the user will be returned.
     * @param id of the user to be found.
     * @return user with id.
     */
    @Override
    public User getUserById(UUID id) {
        Query query = em.createNamedQuery(User.FIND_USER_BY_ID);
        if(id == null) {
            return null;
        }
        query.setParameter("userId", id);
        List<User> queryResult = query.getResultList();
        if(queryResult.size() == 1) {
            System.out.println("Found a user with id: " + id);
            return queryResult.get(0);
        } else {
            System.out.println("Found no users with id: " + id);
            return null;
        }
    }

    /**
     * Search the database for a user with the email. If found the user will be returned.
     * @param email of the user to find in the database.
     * @return user with email.
     */
    @Override
    public User getUserByEmail(String email) {
        Query query = em.createNamedQuery(User.FIND_USER_BY_EMAIL);
        if(email == null) {
            return null;
        }
        email = email.toLowerCase();
        query.setParameter("email", email);
        List<User> queryResult = query.getResultList();
        if(queryResult.size() == 1) {
            System.out.println("Found a user with email: " + email);
            return queryResult.get(0);
        } else {
            System.out.println("Found no users with email: " + email);
            return null;
        }
    }
}
