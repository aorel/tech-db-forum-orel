package api.dao;

import api.models.Post;
import api.models.Thread;

import java.util.List;

public interface ThreadDAO {
    int create(final Thread thread);

    Thread getByIdJoinForum(Integer id);

    Thread getBySlug(String slug);

    Thread getBySlugJoinForum(String slug);

    List<Thread> getByForumSlug(final String slug, final Integer limit, final String since, final Boolean desc);
}
