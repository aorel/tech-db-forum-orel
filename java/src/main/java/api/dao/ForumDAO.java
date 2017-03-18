package api.dao;

import api.models.Forum;
import api.models.User;

import javax.swing.plaf.SliderUI;
import java.util.List;

public interface ForumDAO {
    void create(Forum forum);

    List<Forum> getDuplicates(String slug);

    Forum getBySlug(String slug);

    void getCountPosts(Forum forum);

    void getCountThreads(Forum forum);
}
