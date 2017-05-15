package api.dao.impl;

import api.Settings;
import api.dao.ThreadDAO;
import api.models.Thread;
import api.models.ThreadUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ThreadDAOImpl implements ThreadDAO {

    private static final String SQL_JOIN_FORUM_BEGIN = "SELECT threads.id AS t_id, forums.id AS f_id, forums.slug AS f_slug " +
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
            Timestamp timestamp = Settings.timestampFromString(thread.getCreated());

            SQL = "INSERT INTO threads (title, user_id, forum_id, message, slug, created) VALUES (?, ?, ?, ?, ?, ?) RETURNING id;";
            object = new Object[]{thread.getTitle(), thread.getUserId(), thread.getForumId(), thread.getMessage(), thread.getSlug(), timestamp};
        }

        return template.queryForObject(SQL, object, Integer.class);
    }

    @Override
    public Thread getByIdJoinForum(final Integer id) {
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
    public Thread getById(final Integer id) {
        final String SQL = "SELECT * FROM threads " +
                "WHERE id = ?";
        return template.queryForObject(SQL, THREAD_MAPPER, id);
    }

    @Override
    public Thread getBySlug(final String slug) {
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
        return template.queryForObject(SQL, THREAD_FORUM_USER_MAPPER, slug);
    }

    @Override
    public List<Thread> getByForumSlug(final String slug, final Integer limit, final String since, final Boolean desc) {
        final StringBuilder SQL = new StringBuilder("SELECT threads.id AS t_id, " +
                "threads.title AS t_title, nickname, threads.message AS msg, " +
                "threads.slug AS t_slug, forums.slug AS f_slug, created FROM threads " +
                "JOIN forums ON forums.id=threads.forum_id " +
                "JOIN users ON users.id=threads.user_id " +
                "WHERE LOWER(forums.slug) = LOWER(?)");
        final List<Object> args = new ArrayList<>();
        args.add(slug);



        if (since != null) {
            SQL.append(" AND created ");

            if (desc != null && desc) {
                SQL.append("<= ?");
            } else {
                SQL.append(">= ?");
            }

            Timestamp timestamp = Settings.timestampFromStringZone(since);
            args.add(timestamp);
        }

        SQL.append(" ORDER BY created ");

        if (desc != null && desc) {
            SQL.append(" DESC ");
        }

        SQL.append(" LIMIT ? ");
        args.add(limit);


        return template.query(SQL.toString(), THREAD_FORUM_USER_MAPPER, args.toArray());
    }

    @Override
    public void update(Thread thread, ThreadUpdate threadUpdate) {
        final StringBuilder sql = new StringBuilder("UPDATE threads SET");
        final List<Object> args = new ArrayList<>();

        if (threadUpdate.getTitle() != null) {
            sql.append(" title = ?,");
            args.add(threadUpdate.getTitle());

            thread.setTitle(threadUpdate.getTitle());
        }
        if (threadUpdate.getMessage() != null) {
            sql.append(" message = ?,");
            args.add(threadUpdate.getMessage());

            thread.setMessage(threadUpdate.getMessage());
        }
        if (args.isEmpty()) {
            return;
        }

        sql.deleteCharAt(sql.length() - 1);

        sql.append(" WHERE id = ?;");
        args.add(thread.getId());
        template.update(sql.toString(), args.toArray());
    }

    @Override
    public void getCountVotes(Thread thread) {
        final String SQL = "SELECT count(*) FROM votes " +
                "WHERE thread_id = ?;";

        thread.setVotes(template.queryForObject(SQL, Integer.class, thread.getId()));
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

            Timestamp created = rs.getTimestamp("created");
            thread.setCreated(Settings.DATE_FORMAT.format(created));

            return thread;
        }
    }

    private static final class ThreadForumMapper implements RowMapper<Thread> {
        public Thread mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Thread thread = new Thread();
            thread.setId(rs.getInt("t_id"));
            thread.setForumId(rs.getInt("f_id"));
            thread.setForum(rs.getString("f_slug"));

            return thread;
        }
    }

    private static final class ThreadForumUserMapper implements RowMapper<Thread> {
        public Thread mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Thread thread = new Thread();
            thread.setId(rs.getInt("t_id"));
            thread.setTitle(rs.getString("t_title"));
            thread.setAuthor(rs.getString("nickname"));
            thread.setMessage(rs.getString("msg"));
            thread.setSlug(rs.getString("t_slug"));
            thread.setForum(rs.getString("f_slug"));

            Timestamp created = rs.getTimestamp("created");
            thread.setCreated(Settings.DATE_FORMAT.format(created));

            return thread;
        }
    }
}
