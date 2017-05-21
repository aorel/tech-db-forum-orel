package api.dao;

import api.models.Forum;

public interface ForumDAO {
    void create(final Forum forum);

    Forum getBySlug(final String slug);

    Forum getBySlugJoinUser(final String slug);
}
