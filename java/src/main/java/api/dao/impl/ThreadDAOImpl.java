package api.dao.impl;

import api.dao.ThreadDAO;
import api.models.Forum;
import api.models.Post;
import api.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ThreadDAOImpl implements ThreadDAO {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'");

    private static final String SQL_JOIN_FORUM_BEGIN = "SELECT threads.id AS t_id, forums.slug AS f_slug " +
            "FROM threads " +
            "JOIN forums ON forums.id=threads.forum_id ";
    private static final String SQL_JOIN_ALL_BEGIN = "SELECT threads.id AS t_id, threads.title AS t_title, nickname, threads.message AS msg, " +
            "threads.slug AS t_slug, forums.slug AS f_slug, created FROM threads " +
            "JOIN forums ON forums.id=threads.forum_id " +
            "JOIN users ON users.id=threads.user_id ";

    private static final ThreadMapper THREAD_MAPPER = new ThreadMapper();
    private static final ThreadForumMapper THREAD_FORUM_MAPPER = new ThreadForumMapper();
    private static final ThreadForumUserMapper THREAD_FORUM_USER_MAPPER = new ThreadForumUserMapper();
    @Autowired
    private JdbcTemplate template;

    @Override
    public int create(final Thread thread) {
        String SQL;
        Object[] object;
        if (thread.getCreated() == null) {
            SQL = "INSERT INTO threads (title, user_id, forum_id, message, slug) VALUES (?, ?, ?, ?, ?) RETURNING id;";
            object = new Object[]{thread.getTitle(), thread.getUserId(), thread.getForumId(), thread.getMessage(), thread.getSlug()};
        } else {
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME));

            SQL = "INSERT INTO threads (title, user_id, forum_id, message, slug, created) VALUES (?, ?, ?, ?, ?, ?) RETURNING id;";
            object = new Object[]{thread.getTitle(), thread.getUserId(), thread.getForumId(), thread.getMessage(), thread.getSlug(), timestamp};
        }

        return template.queryForObject(SQL, object, Integer.class);
    }

    @Override
    public Thread getByIdJoinForum(final Integer id) {
        /*final String SQL = "SELECT threads.id AS t_id, forums.slug AS f_slug " +
                "FROM threads " +
                "JOIN forums ON forums.id=threads.forum_id " +
                "WHERE threads.id=?;";*/
        final String SQL = SQL_JOIN_FORUM_BEGIN +
                "WHERE threads.id=?;";
        return template.queryForObject(SQL, THREAD_FORUM_MAPPER, id);
    }

    @Override
    public Thread getByIdJoinAll(final Integer id) {
        final String SQL = SQL_JOIN_ALL_BEGIN +
                "WHERE threads.id=?;";
        return template.queryForObject(SQL, THREAD_FORUM_USER_MAPPER, id);
    }

    @Override
    public Thread getBySlug(String slug) {
        final String SQL = "SELECT * FROM threads " +
                "WHERE LOWER(slug) = LOWER(?)";
        return template.queryForObject(SQL, THREAD_MAPPER, slug);
    }

    @Override
    public Thread getBySlugJoinForum(final String slug) {
        final String SQL = SQL_JOIN_FORUM_BEGIN +
                " WHERE LOWER(threads.slug) = LOWER(?);";
        return template.queryForObject(SQL, THREAD_FORUM_MAPPER, slug);
    }

    @Override
    public Thread getBySlugJoinAll(final String slug) {
        final String SQL = SQL_JOIN_ALL_BEGIN +
                "WHERE LOWER(threads.slug) = LOWER(?)";
        System.out.println(SQL);
        System.out.println(slug);
        return template.queryForObject(SQL, THREAD_FORUM_USER_MAPPER, slug);
    }

    @Override
    public List<Thread> getByForumSlug(final String slug, final Integer limit, final String since, final Boolean desc) {
        final StringBuilder SQL =
                new StringBuilder("SELECT threads.id AS t_id, threads.title AS t_title, nickname, threads.message AS msg, " +
                        "threads.slug AS t_slug, forums.slug AS f_slug, created FROM threads " +
                        "JOIN forums ON forums.id=threads.forum_id " +
                        "JOIN users ON users.id=threads.user_id " +
                        "WHERE LOWER(forums.slug) = LOWER(?)");
        final List<Object> args = new ArrayList<>();
        args.add(slug);

        if (since != null) {
            SQL.append(" AND created ");

            if (desc != null && desc.equals(true)) {
                SQL.append("<= ?");
            } else {
                SQL.append(">= ?");
            }
            args.add(Timestamp.valueOf(LocalDateTime.parse(since, DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        }

        SQL.append(" ORDER BY created ");

        if (desc != null && desc.equals(true)) {
            SQL.append(" DESC ");
        }

        SQL.append(" LIMIT ? ");
        args.add(limit);

        return template.query(SQL.toString(), THREAD_FORUM_USER_MAPPER, args.toArray());
    }

//    public void create(String slug, List<Post> posts) {
//        System.out.println("ThreadDAOImpl create " + slug);
//        for (Post post : posts) {
//
//            System.out.println(post.getAuthor());
//            if(post.getCreated() != null) {
//                System.out.println(post.getCreated());
////                 Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(post.getCreated(), DateTimeFormatter.ISO_DATE_TIME));
//            } else {
//                System.out.println("TIME NULL");
//            }
//            System.out.println(post.getMessage());
//            System.out.println(post.getParent());
//
//            post.setId(42);
//            post.setForum("");
//
//            System.out.println();
//        }
//        System.out.println("---------------------------------------------------");
//    }


    private static final class ThreadMapper implements RowMapper<Thread> {
        public Thread mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Thread thread = new Thread();
            thread.setId(rs.getInt("id"));
            thread.setTitle(rs.getString("title"));
            thread.setUserId(rs.getInt("user_id"));
            thread.setForumId(rs.getInt("forum_id"));
            thread.setMessage(rs.getString("message"));
            thread.setSlug(rs.getString("slug"));

            Timestamp created = rs.getTimestamp("created");
            thread.setCreated(DATE_FORMAT.format(created));


            return thread;
        }
    }

    private static final class ThreadForumMapper implements RowMapper<Thread> {
        public Thread mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Thread thread = new Thread();
            thread.setId(rs.getInt("t_id"));
            thread.setForum(rs.getString("f_slug"));

            return thread;
        }
    }

    private static final class ThreadForumUserMapper implements RowMapper<Thread> {
        public Thread mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Thread thread = new Thread();
            thread.setId(rs.getInt("t_id"));
            thread.setTitle(rs.getString("t_title"));
            //thread.setUserId(rs.getInt("user_id"));
            //thread.setForumId(rs.getInt("forum_id"));
            thread.setAuthor(rs.getString("nickname"));
            thread.setMessage(rs.getString("msg"));
            thread.setSlug(rs.getString("t_slug"));
            thread.setForum(rs.getString("f_slug"));

            Timestamp created = rs.getTimestamp("created");
            thread.setCreated(DATE_FORMAT.format(created));

            return thread;
        }
    }
}
