package api.dao;

import api.models.Forum;
import api.models.User;

import java.util.List;

public interface UserDAO {
    void create(final User user);

    List<User> getDuplicates(final User user);

    User getProfile(final String nickname);

    void setProfile(final User user);

    List<User> getForumUsers(final Forum forum, final Integer limit, final String since, final Boolean desc);
}
