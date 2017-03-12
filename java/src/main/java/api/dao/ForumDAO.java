package api.dao;

import api.models.Forum;
import api.models.User;

import java.util.List;

public interface ForumDAO {
    void create(Forum forum);

    List<Forum> getDuplicates(String slug);

    Forum getSlug(String slug);
}
