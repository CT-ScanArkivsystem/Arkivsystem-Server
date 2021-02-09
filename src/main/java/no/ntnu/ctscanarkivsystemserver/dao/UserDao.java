package no.ntnu.ctscanarkivsystemserver.dao;

import no.ntnu.ctscanarkivsystemserver.model.User;

import java.util.List;
import java.util.UUID;

public interface UserDao {

    User insertUser(User user);

    List<User> selectAllUsers();

    User getUserById(UUID id);

    User getUserByEmail(String email);
}
