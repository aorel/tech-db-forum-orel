package api.dao;

import api.models.Thread;

import java.util.List;

public interface ThreadDAO {
    int create(final Thread thread);

    Thread getByIdJoinForum(Integer id);

    Thread getByIdJoinAll(Integer id);

    Thread getBySlug(String slug);

    Thread getBySlugJoinForum(String slug);

    Thread getBySlugJoinAll(String slug);

    List<Thread> getByForumSlug(final String slug, final Integer limit, final String since, final Boolean desc);
}
