package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
    @Override
    public User insertUser(User user) {
        User newUser = new User(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
        em.persist(newUser);
        em.flush();
        System.out.println("New user id: " + newUser.getUserID());
        return newUser;
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

    @Override
    public User getUserById(UUID id) {
        return null;
    }
}
