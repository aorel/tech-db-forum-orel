package api.dao;

import api.models.Post;
import api.models.Thread;

import java.util.List;

public interface PostDAO {
    void create(Thread thread, List<Post> posts);
}
