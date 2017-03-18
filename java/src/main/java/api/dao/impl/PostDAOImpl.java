package api.dao.impl;

import api.dao.PostDAO;
import api.models.Post;
import api.models.PostUpdate;
import api.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PostDAOImpl implements PostDAO {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'");

    private static final PostMapper POST_MAPPER = new PostMapper();
    private static final ParentMapper PARENT_MAPPER = new ParentMapper();

    @Autowired
    private JdbcTemplate template;

    @Override
    public void create(Thread thread, List<Post> posts) {
        // TODO get user id?
        int id;
        final String SQL = "INSERT INTO posts (parent_id, user_id, forum_id, thread_id, is_edited, message, created) " +
                "VALUES (?, (SELECT id FROM users WHERE nickname = ?), ?, ?, ?, ?, ?) " +
                "RETURNING id;";

        for (Post post : posts) {
            Timestamp timestamp;
            if (post.getCreated() != null) {
                timestamp = Timestamp.valueOf(LocalDateTime.parse(post.getCreated(), DateTimeFormatter.ISO_DATE_TIME));
            } else {
                timestamp = Timestamp.valueOf(LocalDateTime.parse(LocalDateTime.now().toString(), DateTimeFormatter.ISO_DATE_TIME));
            }

            id = template.queryForObject(SQL, Integer.class,
                    post.getParent(), /*post.getUserId()*/ post.getAuthor(), thread.getForumId(),
                    thread.getId(), post.getIsEdited(), post.getMessage(), timestamp);

            post.setId(id);
            post.setThread(thread.getId());
            post.setForum(thread.getForum());
            post.setCreated(DATE_FORMAT.format(timestamp));
        }
    }

    @Override
    public List<Post> getPostsFlat(Thread thread, Integer limit, Integer offset, Boolean desc) {
        final String SQL = "SELECT p.id, parent_id, f.slug, thread_id, nickname, is_edited, p.message, p.created " +
                "FROM posts p " +
                "JOIN threads t ON (p.thread_id = t.id AND t.slug = ?) " +
                "JOIN forums f ON (t.forum_id = f.id)" +
                "JOIN users u ON (u.id = p.user_id) " +
                "ORDER BY created " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?;";

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
                "SELECT tr.id, nickname, tr.created, f.slug, is_edited, tr.message, tr.parent_id, tr.thread_id, array_to_string(z_posts, ' ') AS z_posts FROM tree tr " +
                "JOIN threads t ON (tr.thread_id = t.id AND t.slug = ?) " +
                "JOIN forums f ON (t.forum_id = f.id) " +
                "JOIN users u ON (u.id = tr.user_id) " +
                "ORDER BY z_posts " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?;";

        return template.query(SQL, POST_MAPPER, thread.getSlug(),
                limit, offset);
    }

    @Override
    public List<Integer> getParents(Thread thread, Integer limit, Integer offset, Boolean desc) {
        final String SQL = "SELECT p.id FROM posts p " +
                "JOIN threads t ON (t.id = p.thread_id) " +
                "WHERE parent_id = 0 AND t.slug = ? " +
                "ORDER BY p.id " + (desc ? "DESC" : "ASC") + " LIMIT ? OFFSET ?;";

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
                "SELECT tr.id, nickname, tr.created, f.slug, is_edited, tr.message, tr.parent_id, tr.thread_id, array_to_string(z_posts, ' ') AS z_posts FROM tree tr " +
                "JOIN threads t ON (tr.thread_id = t.id AND t.slug = ?) " +
                "JOIN forums f ON (t.forum_id = f.id) " +
                "JOIN users u ON (u.id = tr.user_id) " +
                "ORDER BY z_posts " + (desc ? "DESC" : "ASC");
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
            post.setCreated(DATE_FORMAT.format(created));

            return post;
        }
    }

    private static final class ParentMapper implements RowMapper<Integer> {
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}
