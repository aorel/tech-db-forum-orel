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
import java.util.List;

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
        final String SQL = "INSERT INTO forums (title, user_id, slug) VALUES(?, ?, ?)";
        template.update(SQL, forum.getTitle(), forum.getUserId(), forum.getSlug());
    }

    @Override
    public List<Forum> getDuplicates(String slug) {
        final String SQL = "SELECT * FROM forums WHERE LOWER(slug) = LOWER(?)";
        return template.query(SQL, FORUM_MAPPER, slug);
    }

    @Override
    public Forum getBySlug(final String slug) {
        final String SQL = "SELECT * FROM forums JOIN users ON users.id=forums.user_id WHERE LOWER(slug) = LOWER(?)";
        return template.queryForObject(SQL, FORUM_USER_MAPPER, slug);
    }

    @Override
    public void getCountPosts(final Forum forum) {
        final String SQL = "SELECT count(*) FROM posts " +
                "WHERE forum_id = ?;";

        forum.setPosts(template.queryForObject(SQL, Integer.class, forum.getId()));
    }

    @Override
    public void getCountThreads(final Forum forum) {
        final String SQL = "SELECT count(*) FROM threads " +
                "WHERE forum_id = ?;";

        forum.setThreads(template.queryForObject(SQL, Integer.class, forum.getId()));
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

            return forum;
        }
    }
}
