package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.User;

import java.util.List;
import java.util.UUID;

public interface UserDao {

    int insertUser(UUID userID, User user);

    default int insertUser(User user) {
        UUID userID = UUID.randomUUID();
        return insertUser(userID, user);
    }

    List<User> selectAllUsers();
}
