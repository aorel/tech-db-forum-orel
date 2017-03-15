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
    private static final ForumMapper FORUM_MAPPER = new ForumMapper();
    private static final ForumUserMapper FORUM_USER_MAPPER = new ForumUserMapper();
    @Autowired
    private JdbcTemplate template;

    @Override
    public void create(Forum forum) {
        final String SQL = "INSERT INTO forums (title, user_id, slug) VALUES(?, ?, ?)";
        template.update(SQL, forum.getTitle(), forum.getUserId(), forum.getSlug());
    }

    @Override
    public List<Forum> getDuplicates(String slug) {
        final String SQL = "SELECT * FROM forums WHERE LOWER(slug) = LOWER(?)";
        return template.query(SQL, FORUM_MAPPER, slug);
    }

    @Override
    public Forum getSlug(String slug) {
        final String SQL = "SELECT * FROM forums JOIN users ON users.id=forums.user_id WHERE LOWER(slug) = LOWER(?)";
        return template.queryForObject(SQL, FORUM_USER_MAPPER, slug);
    }

    private static final class ForumMapper implements RowMapper<Forum> {
        public Forum mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Forum forum = new Forum();
            forum.setId(rs.getInt("id"));
            forum.setTitle(rs.getString("title"));
            //forum.setUser(rs.getString("nickname"));
            forum.setSlug(rs.getString("slug"));
            forum.setUserId(rs.getInt("user_id"));

            return forum;
        }
    }

    private static final class ForumUserMapper implements RowMapper<Forum> {
        public Forum mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Forum forum = new Forum();
            forum.setId(rs.getInt("id"));
            forum.setTitle(rs.getString("title"));
            forum.setUser(rs.getString("nickname"));
            forum.setSlug(rs.getString("slug"));
            forum.setUserId(rs.getInt("user_id"));

            return forum;
        }
    }
}
