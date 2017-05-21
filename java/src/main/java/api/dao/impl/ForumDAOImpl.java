package api.dao.impl;

import api.dao.ForumDAO;
import api.models.Forum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@Transactional
public class ForumDAOImpl implements ForumDAO {
    private static final ForumMapper FORUM_MAPPER = new ForumMapper();
    private static final ForumUserMapper FORUM_USER_MAPPER = new ForumUserMapper();

    private final JdbcTemplate template;

    @Autowired
    public ForumDAOImpl(JdbcTemplate template){
        this.template = template;
    }

    @Override
    public void create(final Forum forum) {
        final String SQL = "INSERT INTO forums (title, user_id, slug) VALUES(?, ?, ?);";
        template.update(SQL, forum.getTitle(), forum.getUserId(), forum.getSlug());
    }

    @Override
    public Forum getBySlug(String slug) {
        final String SQL = "SELECT * FROM forums WHERE LOWER(slug) = LOWER(?);";
        return template.queryForObject(SQL, FORUM_MAPPER, slug);
    }

    @Override
    public Forum getBySlugJoinUser(final String slug) {
        final String SQL = "SELECT * FROM forums f JOIN users u ON u.id=f.user_id WHERE LOWER(slug) = LOWER(?);";
        return template.queryForObject(SQL, FORUM_USER_MAPPER, slug);
    }

    private static final class ForumMapper implements RowMapper<Forum> {
        public Forum mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Forum forum = new Forum();
            forum.setId(rs.getInt("id"));
            forum.setTitle(rs.getString("title"));
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

            forum.setPosts(rs.getInt("__posts"));
            forum.setThreads(rs.getInt("__threads"));

            return forum;
        }
    }
}
