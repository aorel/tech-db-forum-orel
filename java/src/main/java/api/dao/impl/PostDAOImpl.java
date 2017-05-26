package api.dao.impl;

import api.Settings;
import api.dao.PostDAO;
import api.models.Post;
import api.models.PostUpdate;
import api.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
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
    public void create(final Thread thread, final List<Post> posts) throws SQLException {
        final String NEW_SQL = "INSERT INTO posts (parent_id, user_id, forum_id, thread_id, is_edited, message, created, __nickname, __path) " +
                "VALUES (?, (SELECT id FROM users WHERE LOWER(nickname) = LOWER(?)), ?, ?, ?, ?, ?, ?, " +
                "array_append((SELECT __path FROM posts WHERE id = ?), currval('posts_id_seq')::INT));";

        try {
            Connection connection = DataSourceUtils.getConnection(template.getDataSource());

            PreparedStatement preparedStatement = connection.prepareStatement(NEW_SQL, Statement.RETURN_GENERATED_KEYS);

            Timestamp timestamp = Settings.timestampNow();
            SimpleDateFormat dateFormat = new SimpleDateFormat(Settings.DATE_FORMAT_PATTERN_ZULU);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

//            System.out.println("thread posts: size=" + posts.size() + ", date=" + timestamp);
            for (Post post : posts) {
                if (post.getCreated() != null) {
                    timestamp = Settings.timestampFromStringZone(post.getCreated());
                }

                preparedStatement.setInt(1, post.getParent());
                preparedStatement.setString(2, post.getAuthor());
                preparedStatement.setInt(3, thread.getForumId());
                preparedStatement.setInt(4, thread.getId());
                preparedStatement.setBoolean(5, post.getIsEdited());
                preparedStatement.setString(6, post.getMessage());
                preparedStatement.setTimestamp(7, timestamp);

                preparedStatement.setString(8, post.getAuthor());

                preparedStatement.setInt(9, post.getParent());

                preparedStatement.addBatch();

                post.setThread(thread.getId());
                post.setForum(thread.getForum());
                post.setCreated(dateFormat.format(timestamp));
            }

            preparedStatement.executeBatch();

            ResultSet rs = preparedStatement.getGeneratedKeys();
            int i = 0;
            while (rs.next()) {
                int id = rs.getInt(1);
                posts.get(i).setId(id);
                i++;
            }

            preparedStatement.close();
        } catch (BatchUpdateException e) {
            System.out.println("PreparedStatement BatchUpdateException");
            throw e;
        } catch (SQLException e) {
//            e.printStackTrace();
            throw e;
        }

        final String SQL_UP_FORUM = "UPDATE forums SET __posts = __posts + ? WHERE id = ?;";
        template.update(SQL_UP_FORUM, posts.size(), thread.getForumId());
    }

    @Override
    public List<Post> getPostsFlat(final Thread thread, final Integer limit, final Integer offset, final Boolean desc) {
        final String SQL = "SELECT p.id, parent_id, f.slug, thread_id, __nickname, is_edited, p.message, p.created " +
                "FROM posts p " +
                "JOIN forums f ON (f.id = p.forum_id) " +
                "WHERE p.thread_id = ? " +
                "ORDER BY created " + (desc ? "DESC" : "ASC") + ", id " + (desc ? "DESC" : "ASC") + " " +
                "LIMIT ? OFFSET ?;";

        return template.query(SQL, POST_MAPPER, thread.getId(),
                limit, offset);
    }

    @Override
    public List<Post> getPostsTree(final Thread thread, final Integer limit, final Integer offset, final Boolean desc) {
        final String SQL = "SELECT p.id, parent_id, f.slug, thread_id, __nickname, is_edited, p.message, p.created " +
                "FROM posts p " +
                "JOIN forums f ON (f.id = p.forum_id) " +
                "WHERE p.thread_id = ? " +
                "ORDER BY p.__path " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?;";

        return template.query(SQL, POST_MAPPER, thread.getId(),
                limit, offset);
    }

    @Override
    public List<Integer> getParents(final Thread thread, final Integer limit, final Integer offset, final Boolean desc) {
        final String SQL = "SELECT p.id " +
                "FROM posts p " +
                "WHERE parent_id = 0 AND p.thread_id = ? " +
                "ORDER BY p.id " + (desc ? "DESC" : "ASC") + ", id LIMIT ? OFFSET ?;";
        return template.query(SQL, PARENT_MAPPER, thread.getId(),
                limit, offset);
    }

    @Override
    public List<Post> getPostsParentTree(final Thread thread, final Boolean desc, final List<Integer> parents) {
        final String SQL = "SELECT p.id, parent_id, f.slug, thread_id, __nickname, is_edited, p.message, p.created " +
                "FROM posts p " +
                "JOIN forums f ON (f.id = p.forum_id) " +
                "WHERE p.__path[1] = ? AND p.thread_id = ? " +
                "ORDER BY __path " + (desc ? "DESC" : "ASC") + ", p.id " + (desc ? "DESC" : "ASC") + ";";

        List<Post> result = new ArrayList<>();
        for (Integer parent : parents) {
            result.addAll(template.query(SQL, POST_MAPPER, parent, thread.getId()));
        }
        return result;
    }

    @Override
    public Post getById(final Integer id) {
        final String SQL = "SELECT p.id, parent_id, f.slug, thread_id, __nickname, is_edited, p.message, p.created " +
                "FROM posts p " +
                "JOIN forums f ON (f.id = p.forum_id) " +
                "WHERE p.id = ?;";
        return template.queryForObject(SQL, POST_MAPPER, id);
    }

    @Override
    public void update(final Post post, final PostUpdate postUpdate) {
        if(post.getMessage().equals(postUpdate.getMessage())){
            return;
        }

        final String SQL = "UPDATE posts SET message = ?, is_edited = TRUE WHERE id = ?;";
        template.update(SQL, postUpdate.getMessage(), post.getId());

        post.setMessage(postUpdate.getMessage());
        post.setIsEdited(true);
    }

    @Override
    public List<Integer> getChildren(final Integer thread_id) {
        final String SQL = "SELECT id FROM posts WHERE thread_id=?;";
        return template.queryForList(SQL, Integer.class, thread_id);
    }

    private static final class PostMapper implements RowMapper<Post> {
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Post post = new Post();
            post.setId(rs.getInt("id"));
            post.setParent(rs.getInt("parent_id"));
            post.setForum(rs.getString("slug"));
            post.setThread(rs.getInt("thread_id"));
            post.setAuthor(rs.getString("__nickname"));
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
