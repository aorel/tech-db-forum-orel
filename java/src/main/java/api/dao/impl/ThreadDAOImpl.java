package api.dao.impl;

import api.Settings;
import api.dao.ThreadDAO;
import api.models.Forum;
import api.models.Thread;
import api.models.ThreadUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Repository
@Transactional
public class ThreadDAOImpl implements ThreadDAO {
    private static final String SQL_JOIN_FORUM_BEGIN = "SELECT t.id AS t_id, f.id AS f_id, f.slug AS f_slug " +
            "FROM threads t " +
            "JOIN forums f ON f.id=t.forum_id ";

    private static final String SQL_JOIN_ALL_BEGIN = "SELECT t.id AS t_id, t.title AS t_title, nickname, t.message AS msg, " +
            "t.slug AS t_slug, f.slug AS f_slug, created, __votes " +
            "FROM threads t " +
            "JOIN forums f ON f.id=t.forum_id " +
            "JOIN users u ON u.id=t.user_id ";

    private static final ThreadForumMapper THREAD_FORUM_MAPPER = new ThreadForumMapper();
    private static final ThreadForumUserMapper THREAD_FORUM_USER_MAPPER = new ThreadForumUserMapper();

    private final JdbcTemplate template;

    @Autowired
    public ThreadDAOImpl(JdbcTemplate template){
        this.template = template;
    }

    @Override
    public void create(final Thread thread) {
        final String SQL;
        Object[] object;
        if (thread.getCreated() == null) {
            SQL = "INSERT INTO threads (title, user_id, forum_id, message, slug) VALUES (?, ?, ?, ?, ?) RETURNING id;";
            object = new Object[]{thread.getTitle(), thread.getUserId(), thread.getForumId(), thread.getMessage(), thread.getSlug()};
        } else {
            Timestamp timestamp = Settings.timestampFromStringZone(thread.getCreated());

            SQL = "INSERT INTO threads (title, user_id, forum_id, message, slug, created) VALUES (?, ?, ?, ?, ?, ?) RETURNING id;";
            object = new Object[]{thread.getTitle(), thread.getUserId(), thread.getForumId(), thread.getMessage(), thread.getSlug(), timestamp};
        }
        thread.setId(template.queryForObject(SQL, object, Integer.class));

        final String SQL_UP_FORUM = "UPDATE forums SET __threads = __threads + 1 WHERE id = ?;";
        template.update(SQL_UP_FORUM, thread.getForumId());
    }

    @Override
    public Thread getByIdJoinForum(final Integer id) {
        final String SQL = SQL_JOIN_FORUM_BEGIN +
                "WHERE t.id=?;";
        return template.queryForObject(SQL, THREAD_FORUM_MAPPER, id);
    }

    @Override
    public Thread getByIdJoinAll(final Integer id) {
        final String SQL = SQL_JOIN_ALL_BEGIN +
                "WHERE t.id=?;";
        return template.queryForObject(SQL, THREAD_FORUM_USER_MAPPER, id);
    }

    @Override
    public Thread getBySlugJoinForum(final String slug) {
        final String SQL = SQL_JOIN_FORUM_BEGIN +
                "WHERE LOWER(t.slug) = LOWER(?);";
        return template.queryForObject(SQL, THREAD_FORUM_MAPPER, slug);
    }

    @Override
    public Thread getBySlugJoinAll(final String slug) {
        final String SQL = SQL_JOIN_ALL_BEGIN +
                "WHERE LOWER(t.slug) = LOWER(?);";
        return template.queryForObject(SQL, THREAD_FORUM_USER_MAPPER, slug);
    }

    @Override
    public List<Thread> getByForum(final Forum forum, final Integer limit, final String since, final Boolean desc) {
        final String SQL_BEGIN = "SELECT t.id AS t_id, " +
                "t.title AS t_title, nickname, t.message AS msg, " +
                "t.slug AS t_slug, f.slug AS f_slug, created, __votes " +
                "FROM threads t " +
                "JOIN forums f ON f.id=t.forum_id " +
                "JOIN users u ON u.id=t.user_id " +
                "WHERE f.id = ? ";

        final StringBuilder sql = new StringBuilder(SQL_BEGIN);

        final List<Object> args = new ArrayList<>();
        args.add(forum.getId());


        if (since != null) {
            sql.append("AND created ");

            if (desc != null && desc) {
                sql.append("<= ? ");
            } else {
                sql.append(">= ? ");
            }

            Timestamp timestamp = Settings.timestampFromStringZone(since);
            args.add(timestamp);
        }

        sql.append("ORDER BY created ");

        if (desc != null && desc) {
            sql.append("DESC ");
        }

        sql.append("LIMIT ?;");
        args.add(limit);


        return template.query(sql.toString(), THREAD_FORUM_USER_MAPPER, args.toArray());
    }

    @Override
    public void update(final Thread thread, final ThreadUpdate threadUpdate) {
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
            SimpleDateFormat dateFormat = new SimpleDateFormat(Settings.DATE_FORMAT_PATTERN_ZULU);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            thread.setCreated(dateFormat.format(created));

            thread.setVotes(rs.getInt("__votes"));

            return thread;
        }
    }
}
