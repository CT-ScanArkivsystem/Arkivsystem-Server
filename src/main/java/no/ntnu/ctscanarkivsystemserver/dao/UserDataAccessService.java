package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.Role;
import no.ntnu.ctscanarkivsystemserver.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

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
    public User insertUser(User user) {
        Role userRole = em.find(Role.class, Role.USER);
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

    @Override
    public User getUserById(UUID id) {
        return null;
    }

    /**
     * Search the database for a user with the email parameter. If found the user will be returned.
     * @param email of the user to find in the database.
     * @return user with email equal to parameter.
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
