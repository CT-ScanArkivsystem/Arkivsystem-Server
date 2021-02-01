package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository("postgreSQL")
public class UserDataAccessService implements UserDao{

    private static List<User> DB = new ArrayList<>();

    @Override
    public int insertUser(UUID userID, User user) {
        DB.add(new User(userID, user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword()));
        return 1;
    }

    @Override
    public List<User> selectAllUsers() {
        return DB;
    }
}
