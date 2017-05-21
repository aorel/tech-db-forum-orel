package api.dao;

import api.models.Post;
import api.models.PostUpdate;
import api.models.Thread;

import java.sql.SQLException;
import java.util.List;

public interface PostDAO {
    void create(final Thread thread, final List<Post> posts) throws SQLException;

    List<Post> getPostsFlat(final Thread thread, final Integer limit, final Integer offset, final Boolean desc);

    List<Post> getPostsTree(final Thread thread, final Integer limit, final Integer offset, final Boolean desc);

    List<Integer> getParents(final Thread thread, final Integer limit, final Integer offset, final Boolean desc);

    List<Post> getPostsParentTree(final Thread thread, final Boolean desc, final List<Integer> parents);

    Post getById(final Integer id);

    void update(final Post post, final PostUpdate postUpdate);

    List<Integer> getChildren(final Integer thread_id);
}
