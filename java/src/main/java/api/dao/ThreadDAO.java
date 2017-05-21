package api.dao;

import api.models.Forum;
import api.models.Thread;
import api.models.ThreadUpdate;

import java.util.List;

public interface ThreadDAO {
    void create(final Thread thread);

    Thread getByIdJoinForum(final Integer id);

    Thread getByIdJoinAll(final Integer id);

    Thread getBySlugJoinForum(final String slug);

    Thread getBySlugJoinAll(final String slug);

    List<Thread> getByForumSlug(final String slug, final Integer limit, final String since, final Boolean desc);

    void update(final Thread thread, final ThreadUpdate threadUpdate);
}
