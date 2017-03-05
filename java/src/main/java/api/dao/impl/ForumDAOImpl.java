package api.dao.impl;

import api.dao.ForumDAO;
import api.models.Forum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class ForumDAOImpl implements ForumDAO {
    @Autowired
    private JdbcTemplate template;

    @Override
    public void create(Forum forum) {
        String SQL = "INSERT INTO forums ('title', 'user', 'slug') VALUES(?, ?, ?)";
        template.update(SQL, new Object[]{forum.getTitle(), forum.getUser(), forum.getSlug()});
    }

    @Override
    public List<Forum> getDuplicates(Forum forum) {
        return null;
    }
}
