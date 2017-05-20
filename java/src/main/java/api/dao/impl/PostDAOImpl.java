package api.dao.impl;

import api.Settings;
import api.dao.PostDAO;
import api.models.Post;
import api.models.PostUpdate;
import api.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Repository
@Transactional
public class PostDAOImpl implements PostDAO {
    private static final PostMapper POST_MAPPER = new PostMapper();
    private static final ParentMapper PARENT_MAPPER = new ParentMapper();

    private final JdbcTemplate template;

    @Autowired
    public PostDAOImpl(JdbcTemplate template){
        this.template = template;
    }

    @Override
    public void create(Thread thread, List<Post> posts) throws SQLException {
        // TODO get user id?
        final String NEW_SQL = "INSERT INTO posts (id, parent_id, user_id, forum_id, thread_id, is_edited, message, created) " +
                "VALUES (?, ?, (SELECT id FROM users WHERE nickname = ?), ?, ?, ?, ?, ?);";

        try (Connection connection = template.getDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(NEW_SQL, Statement.NO_GENERATED_KEYS);

            Timestamp timestamp = Settings.timestampNow();
            SimpleDateFormat dateFormat = new SimpleDateFormat(Settings.DATE_FORMAT_PATTERN_ZULU);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

//            System.out.println("thread posts: size=" + posts.size() + ", date=" + timestamp);
            for (Post post : posts) {
                // TODO generate next post.size() id!!!
                final Integer new_id = template.queryForObject("SELECT nextval('posts_id_seq')", Integer.class);

                if (post.getCreated() != null) {
                    timestamp = Settings.timestampFromString(post.getCreated());
                }

                preparedStatement.setInt(1, new_id);
                preparedStatement.setInt(2, post.getParent());
                preparedStatement.setString(3, post.getAuthor());
                preparedStatement.setInt(4, thread.getForumId());
                preparedStatement.setInt(5, thread.getId());
                preparedStatement.setBoolean(6, post.getIsEdited());
                preparedStatement.setString(7, post.getMessage());
                preparedStatement.setTimestamp(8, timestamp);
                preparedStatement.addBatch();
                post.setId(new_id);
                post.setThread(thread.getId());
                post.setForum(thread.getForum());
                post.setCreated(dateFormat.format(timestamp));
            }

            preparedStatement.executeBatch();
            preparedStatement.close();
        } catch (SQLException e) {
            // TODO try with res
            System.out.println("preparedStatement error");
            throw e;
        }
    }

    @Override
    public List<Post> getPostsFlat(Thread thread, Integer limit, Integer offset, Boolean desc) {
        final String SQL = "SELECT p.id, parent_id, f.slug, thread_id, nickname, is_edited, p.message, p.created " +
                "FROM posts p " +
                "JOIN threads t ON (p.thread_id = t.id AND t.slug = ?) " +
                "JOIN forums f ON (t.forum_id = f.id)" +
                "JOIN users u ON (u.id = p.user_id) " +
                "ORDER BY created " + (desc ? "DESC" : "ASC") + ", id " + (desc ? "DESC" : "ASC") + " " +
                "LIMIT ? OFFSET ?;";

        return template.query(SQL, POST_MAPPER, thread.getSlug(),
                limit, offset);
    }

    @Override
    public List<Post> getPostsTree(Thread thread, Integer limit, Integer offset, Boolean desc) {
        final String SQL = "WITH RECURSIVE tree (id, user_id, created, forum_id, is_edited, message, parent_id, thread_id, z_posts) AS ( " +
                "SELECT id, user_id, created, forum_id, is_edited, message, parent_id, thread_id, array[id] FROM posts WHERE parent_id = 0 " +
                "UNION ALL " +
                "SELECT p.id, p.user_id, p.created, p.forum_id, p.is_edited, p.message, p.parent_id, p.thread_id, array_append(z_posts, p.id) FROM posts p " +
                "JOIN tree ON tree.id = p.parent_id) " +
                "SELECT tr.id, nickname, tr.created, f.slug, is_edited, tr.message, tr.parent_id, tr.thread_id, z_posts AS z_posts FROM tree tr " +
                "JOIN threads t ON (tr.thread_id = t.id AND t.slug = ?) " +
                "JOIN forums f ON (t.forum_id = f.id) " +
                "JOIN users u ON (u.id = tr.user_id) " +
                "ORDER BY z_posts " + (desc ? "DESC" : "ASC") + ", id " + (desc ? "DESC" : "ASC") + " " +
                "LIMIT ? OFFSET ?;";

        return template.query(SQL, POST_MAPPER, thread.getSlug(),
                limit, offset);
    }

    @Override
    public List<Integer> getParents(Thread thread, Integer limit, Integer offset, Boolean desc) {
        final String SQL = "SELECT p.id FROM posts p " +
                "JOIN threads t ON (t.id = p.thread_id) " +
                "WHERE parent_id = 0 AND t.slug = ? " +
                "ORDER BY p.id " + (desc ? "DESC" : "ASC") + ", id LIMIT ? OFFSET ?;";

        return template.query(SQL, PARENT_MAPPER, thread.getSlug(),
                limit, offset);
    }

    @Override
    public List<Post> getPostsParentTree(Thread thread, Boolean desc, List<Integer> parents) {
        List<Post> result = new ArrayList<>();

        final String SQL = "WITH RECURSIVE tree (id, user_id, created, forum_id, is_edited, message, parent_id, thread_id, z_posts) AS ( " +
                "SELECT id, user_id, created, forum_id, is_edited, message, parent_id, thread_id, array[id] FROM posts WHERE id = ? " +
                "UNION ALL " +
                "SELECT p.id, p.user_id, p.created, p.forum_id, p.is_edited, p.message, p.parent_id, p.thread_id, array_append(z_posts, p.id) FROM posts p " +
                "JOIN tree ON tree.id = p.parent_id) " +
                "SELECT tr.id, nickname, tr.created, f.slug, is_edited, tr.message, tr.parent_id, tr.thread_id, z_posts AS z_posts FROM tree tr " +
                "JOIN threads t ON (tr.thread_id = t.id AND t.slug = ?) " +
                "JOIN forums f ON (t.forum_id = f.id) " +
                "JOIN users u ON (u.id = tr.user_id) " +
                "ORDER BY z_posts " + (desc ? "DESC" : "ASC") + ", id " + (desc ? "DESC" : "ASC");
        for (Integer parent : parents) {
            result.addAll(template.query(SQL, POST_MAPPER, parent, thread.getSlug()));
        }
        return result;
    }

    @Override
    public Post getById(Integer id) {
        final String SQL = "SELECT p.id, parent_id, f.slug, thread_id, nickname, is_edited, p.message, p.created " +
                "FROM posts p " +
                "JOIN threads t ON (p.thread_id = t.id AND p.id=?) " +
                "JOIN forums f ON (t.forum_id = f.id) " +
                "JOIN users u ON (u.id = p.user_id);";
        return template.queryForObject(SQL, POST_MAPPER, id);
    }

    @Override
    public void update(Post post, PostUpdate postUpdate) {
        if(post.getMessage().equals(postUpdate.getMessage())){
            return;
        }

        final String SQL = "UPDATE posts SET " +
                "message = ?, " +
                "is_edited = TRUE " +
                "WHERE id = ?;";
        template.update(SQL, postUpdate.getMessage(), post.getId());

        post.setMessage(postUpdate.getMessage());
        post.setIsEdited(true);
    }

    @Override
    public List<Integer> getChildren(Integer thread_id) {
        final String SQL = "SELECT id " +
                "FROM posts " +
                "WHERE thread_id=?;";
        return template.queryForList(SQL, Integer.class, thread_id);
    }

    private static final class PostMapper implements RowMapper<Post> {
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Post post = new Post();
            post.setId(rs.getInt("id"));
            post.setParent(rs.getInt("parent_id"));
            post.setForum(rs.getString("slug"));
            post.setThread(rs.getInt("thread_id"));
            post.setAuthor(rs.getString("nickname"));
            post.setIsEdited(rs.getBoolean("is_edited"));
            post.setMessage(rs.getString("message"));

            Timestamp created = rs.getTimestamp("created");
            SimpleDateFormat dateFormat = new SimpleDateFormat(Settings.DATE_FORMAT_PATTERN_ZULU);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            post.setCreated(dateFormat.format(created));

            return post;
        }
    }

    private static final class ParentMapper implements RowMapper<Integer> {
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}
