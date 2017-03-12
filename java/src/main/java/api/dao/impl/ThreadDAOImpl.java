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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ThreadDAOImpl implements ThreadDAO {
    private static final ThreadMapper THREAD_MAPPER = new ThreadMapper();
    @Autowired
    private JdbcTemplate template;

    @Override
    public int create(final Thread thread) {
        String SQL;
        Object[] object;
        // .isEmpty()
        if(thread.getCreated() == null) {
            SQL = "INSERT INTO threads (title, user_id, forum_id, messege, slug) VALUES (?, ?, ?, ?, ?) RETURNING id;";
            object = new Object[]{thread.getTitle(), thread.getUserId(), thread.getForumId(), thread.getMessage(), thread.getSlug()};
        } else {
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME));
            SQL = "INSERT INTO threads (title, user_id, forum_id, messege, slug, created) VALUES (?, ?, ?, ?, ?, ?) RETURNING id;";
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
        //final String SQL = "SELECT * FROM forums WHERE LOWER(slug) = LOWER(?)";
        //return template.query(SQL, FORUM_MAPPER, slug);

        final StringBuilder sql = new StringBuilder("SELECT * FROM threads WHERE LOWER(forum) = LOWER(?)");
        final List<Object> args = new ArrayList<>();
        args.add(slug);

        if (since != null) {
            sql.append(" AND created ");

            if (desc.equals(true)) {
                sql.append("<= ?");
            } else {
                sql.append(">= ?");
            }
            args.add(Timestamp.valueOf(LocalDateTime.parse(since, DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        }

        sql.append(" ORDER BY created ");

        if (desc.equals(true)) {
            sql.append(" DESC ");
        }

        sql.append(" LIMIT ? ");
        args.add(limit);
        return template.query(sql.toString(), THREAD_MAPPER, args.toArray());
    }


    private static final class ThreadMapper implements RowMapper<Thread> {
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
    }
}
