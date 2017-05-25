package api.dao.impl;

import api.dao.UserDAO;
import api.models.Forum;
import api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class UserDAOImpl implements UserDAO {
    private static final UserMapper USER_MAPPER = new UserMapper();

    private final JdbcTemplate template;

    @Autowired
    public UserDAOImpl(JdbcTemplate template){
        this.template = template;
    }

    @Override
    public void create(final User user) {
        final String SQL = "INSERT INTO users (nickname, fullname, email, about) VALUES(?, ?, ?, ?)";
        template.update(SQL, user.getNickname(), user.getFullname(), user.getEmail(), user.getAbout());
    }

    @Override
    public List<User> getDuplicates(final User user) {
        final String SQL = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?) OR LOWER(email) = LOWER(?)";
        return template.query(SQL, USER_MAPPER, user.getNickname(), user.getEmail());
    }

    @Override
    public User getProfile(final String nickname) {
        final String SQL = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)";
        return template.queryForObject(SQL, USER_MAPPER, nickname);
    }

    @Override
    public void setProfile(final User user) {
        final StringBuilder sql = new StringBuilder("UPDATE users SET");
        final List<Object> args = new ArrayList<>();

        if (user.getFullname() != null && !user.getFullname().isEmpty()) {
            sql.append(" fullname = ?,");
            args.add(user.getFullname());
        }

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            sql.append(" email = ?,");
            args.add(user.getEmail());
        }

        if (user.getAbout() != null && !user.getAbout().isEmpty()) {
            sql.append(" about = ?,");
            args.add(user.getAbout());
        }

        if (args.isEmpty()) {
            return;
        }

        sql.deleteCharAt(sql.length() - 1);
        sql.append(" WHERE LOWER(nickname) = LOWER(?);");
        args.add(user.getNickname());
        template.update(sql.toString(), args.toArray());
    }

    @Override
    public List<User> getForumUsers(final Forum forum, final Integer limit, final String since, final Boolean desc) {
        final String SQL_BEGIN = "SELECT u.id, nickname, fullname, email, about " +
                "FROM users u " +
                "WHERE u.id IN (" +
                "SELECT user_id " +
                "FROM forum_users " +
                "WHERE forum_id = ?" +
                ") ";
        final StringBuilder sql = new StringBuilder(SQL_BEGIN);

        final List<Object> args = new ArrayList<>();
        args.add(forum.getId());

        if (since != null) {
            if (desc != null && desc) {
                sql.append("AND LOWER(nickname) < LOWER(?) ");
            } else {
                sql.append("AND LOWER(nickname) > LOWER(?) ");
            }
            args.add(since);
        }

        sql.append("ORDER BY nickname ");
        if (desc != null) {
            sql.append((desc ? "DESC " : "ASC "));
        }
        if (limit != null) {
            sql.append("LIMIT ? ");
            args.add(limit);
        }

        sql.append(";");

        return template.query(sql.toString(), args.toArray(), USER_MAPPER);
    }

    private static final class UserMapper implements RowMapper<User> {
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            final User user = new User();
            user.setId(rs.getInt("id"));
            user.setNickname(rs.getString("nickname"));
            user.setFullname(rs.getString("fullname"));
            user.setEmail(rs.getString("email"));
            user.setAbout(rs.getString("about"));

            return user;
        }
    }
}
