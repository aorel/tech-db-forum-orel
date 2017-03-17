package api.dao;

import api.models.Post;
import api.models.PostUpdate;
import api.models.Thread;

import java.util.List;

public interface PostDAO {
    void create(Thread thread, List<Post> posts);

    List<Post> getPostsFlat(Thread thread, Integer limit, Integer offset, Boolean desc);

    List<Post> getPostsTree(Thread thread, Integer limit, Integer offset, Boolean desc);

    List<Integer> getParents(Thread thread, Integer limit, Integer offset, Boolean desc);

    List<Post> getPostsParentTree(Thread thread, Boolean desc, List<Integer> parents);

    Post getById(Integer id);

    void update(Post post, PostUpdate postUpdate);

    List<Integer> getChildren(Integer thread_id);
}
