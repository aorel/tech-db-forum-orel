package api.dao.impl;

import api.dao.PostDAO;
import api.models.Post;
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
import java.util.List;

@Repository
public class PostDAOImpl implements PostDAO {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'");

    private static final PostMapper POST_MAPPER = new PostMapper();

    @Autowired
    private JdbcTemplate template;

    public void create(Thread thread, List<Post> posts) {
        System.out.println("PostDAOImpl create " +
                thread.getSlug() +
                " [" + thread.getId() + "]");
        System.out.println("  Forum " + thread.getForum());
        System.out.println("ForumId " + thread.getForumId());

        // TODO get user id?
        int id;
        final String SQL = "INSERT INTO posts (parent_id, user_id, forum_id, thread_id, is_edited, message, created) " +
                "VALUES (?, (SELECT id FROM users WHERE nickname = ?), ?, ?, ?, ?, ?) " +
                "RETURNING id;";

        for (Post post : posts) {


            Timestamp timestamp;
            if (post.getCreated() != null) {
                System.out.println(post.getCreated());
                timestamp = Timestamp.valueOf(LocalDateTime.parse(post.getCreated(), DateTimeFormatter.ISO_DATE_TIME));
            } else {
                System.out.println("TIME NULL");
                timestamp = Timestamp.valueOf(LocalDateTime.parse(LocalDateTime.now().toString(), DateTimeFormatter.ISO_DATE_TIME));

            }
            System.out.println(" Author " + post.getAuthor());
            System.out.println(" UserId " + post.getUserId());
            System.out.println("Message " + post.getMessage());
            System.out.println(" Parent " + post.getParent());
            System.out.println("Created " + DATE_FORMAT.format(timestamp));
            System.out.println("Created " + DATE_FORMAT.format(timestamp));

            id = template.queryForObject(SQL, Integer.class,
                    post.getParent(), /*post.getUserId()*/ post.getAuthor(), thread.getForumId(),
                    thread.getId(), post.getIsEdited(), post.getMessage(), timestamp);


            post.setId(id);
            post.setThread(thread.getId());
            post.setForum(thread.getForum());
            post.setCreated(DATE_FORMAT.format(timestamp));

            System.out.println("-");
        }
        System.out.println("---------------------------------------------------");
    }

    public List<Post> getPosts(Thread thread, Integer limit, Integer offset, String sort, Boolean desc) {
        final String SQL = "SELECT p.id, parent_id, f.slug, thread_id, nickname, is_edited, p.message, p.created " +
                "FROM posts p " +
                "JOIN threads t ON (p.thread_id = t.id AND t.slug = ?) " +
                "JOIN forums f ON (t.forum_id = f.id)" +
                "JOIN users u ON (u.id = p.user_id) " +
                "ORDER BY created " + (desc ? "DESC" : "ASC" ) + " LIMIT ? OFFSET ?;";

        return template.query(SQL, POST_MAPPER, thread.getSlug(),
                limit, offset);
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
}
