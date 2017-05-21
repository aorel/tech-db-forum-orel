package api.dao;

import api.models.Forum;

import java.util.List;

public interface ForumDAO {
    void create(final Forum forum);

    List<Forum> getDuplicates(final String slug);

    Forum getBySlug(final String slug);
}
