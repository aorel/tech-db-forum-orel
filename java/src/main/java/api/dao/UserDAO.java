package api.dao;

import api.models.User;

import java.util.List;

public interface UserDAO {
    void create(User user);

    List<User> getDuplicates(User user);

    User getProfile(String nickname);

    void setProfile(User user);
}
