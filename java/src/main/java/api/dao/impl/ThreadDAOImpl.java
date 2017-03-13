package api.dao.impl;

import api.dao.ThreadDAO;
import api.models.Forum;
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
    //private static final ThreadMapper THREAD_MAPPER = new ThreadMapper();
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

    /*@Override
    public List<Thread> getDuplicates(Thread thread) {
//        final String SQL = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?) OR LOWER(email) = LOWER(?)";
//        return template.query(SQL,
//                new Object[]{user.getNickname(), user.getEmail()},
//                new UserDAOImpl.UserMapper());

//        final String SQL = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?) OR LOWER(email) = LOWER(?)";
//        return template.query(SQL,
//                new Object[]{user.getNickname(), user.getEmail()},
//                new UserDAOImpl.UserMapper());


        return null;
    }*/

    @Override
    public List<Thread> get(final String slug, final Integer limit, final String since, final Boolean desc) {
        final StringBuilder sql = new StringBuilder(
                "SELECT threads.id AS t_id, threads.title AS t_title, nickname, threads.message AS msg, " +
                        "threads.slug AS t_slug, forums.slug AS f_slug, created FROM threads " +
                        "JOIN forums ON forums.id=threads.forum_id " +
                        "JOIN users ON users.id=threads.user_id " +
                        "WHERE LOWER(forums.slug) = LOWER(?)"
        );
        final List<Object> args = new ArrayList<>();
        args.add(slug);

        if (since != null) {
            sql.append(" AND created ");

            if (desc != null && desc.equals(true)) {
                sql.append("<= ?");
            } else {
                sql.append(">= ?");
            }
            args.add(Timestamp.valueOf(LocalDateTime.parse(since, DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        }

        sql.append(" ORDER BY created ");

        if (desc != null && desc.equals(true)) {
            sql.append(" DESC ");
        }

        sql.append(" LIMIT ? ");
        args.add(limit);

        return template.query(sql.toString(), THREAD_FORUM_USER_MAPPER, args.toArray());
    }


    /*private static final class ThreadMapper implements RowMapper<Thread> {
        public Thread mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Thread thread = new Thread();
            thread.setId(rs.getInt("id"));
            thread.setTitle(rs.getString("title"));
            thread.setUserId(rs.getInt("user_id"));
            thread.setForumId(rs.getInt("forum_id"));
            thread.setMessage(rs.getString("message"));
            thread.setSlug(rs.getString("slug"));
            thread.setCreated(rs.getString("created"));

            return thread;
        }
    }*/

    private static final class ThreadForumUserMapper implements RowMapper<Thread> {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'");

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
            thread.setCreated(dateFormat.format(created));

            return thread;
        }
    }
}
