package api.dao.impl;

import api.dao.ForumDAO;
import api.models.Forum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ForumDAOImpl implements ForumDAO {
    @Autowired
    private JdbcTemplate template;

    @Override
    public void create(Forum forum) {
        final String SQL = "INSERT INTO forums (title, user_id, slug) VALUES(?, ?, ?)";
        template.update(SQL, new Object[]{forum.getTitle(), forum.getUserId(), forum.getSlug()});
    }

    @Override
    public List<Forum> getDuplicates(Forum forum) {
        final String SQL = "SELECT * FROM forums WHERE LOWER(slug) = LOWER(?)";
        return template.query(SQL,
                new Object[]{forum.getSlug()},
                new ForumMapper());
    }

    @Override
    public Forum getSlug(String slug) {
//        final String SQL = "SELECT * FROM forums WHERE LOWER(slug) = LOWER(?)";
        final String SQL = "SELECT * FROM forums JOIN users ON users.id=forums.user_id WHERE LOWER(slug) = LOWER(?)";
        return template.queryForObject(SQL, new Object[]{slug}, new ForumMapper());
    }

    private static final class ForumMapper implements RowMapper<Forum> {
        public Forum mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Forum forum = new Forum(rs.getString("title"),
                    rs.getString("nickname"),
                    rs.getString("slug"));
            return forum;
        }
    }
}
