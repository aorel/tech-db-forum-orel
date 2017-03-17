package api.dao.impl;

import api.dao.UserDAO;
import api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserDAOImpl implements UserDAO {
    private static final UserMapper USER_MAPPER = new UserMapper();

    @Autowired
    private JdbcTemplate template;

    @Override
    public void create(User user) {
        final String SQL = "INSERT INTO users (nickname, fullname, email, about) VALUES(?, ?, ?, ?)";
        template.update(SQL, user.getNickname(), user.getFullname(), user.getEmail(), user.getAbout());
    }

    @Override
    public List<User> getDuplicates(User user) {
        final String SQL = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?) OR LOWER(email) = LOWER(?)";
        return template.query(SQL, USER_MAPPER, user.getNickname(), user.getEmail());
    }

    @Override
    public User getProfile(String nickname) {
        final String SQL = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)";
        return template.queryForObject(SQL, USER_MAPPER, nickname);
    }

    @Override
    public void setProfile(User user) {
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
        sql.append(" WHERE LOWER(nickname) = LOWER(?)");
        args.add(user.getNickname());
        template.update(sql.toString(), args.toArray());
    }

    @Override
    public List<User> getForumUsers(String slug, Integer limit, String since, Boolean desc) {
        final StringBuilder SQL = new StringBuilder("SELECT DISTINCT u.id, nickname, fullname, email, about FROM users u " +
                "LEFT JOIN threads t ON (u.id = t.user_id) " +
                "LEFT JOIN posts p ON (u.id = p.user_id) " +
                "JOIN forums f ON (LOWER(f.slug)=LOWER(?) AND (f.id = t.forum_id OR f.id = p.forum_id)) ");
        final List<Object> args = new ArrayList<>();
        args.add(slug);

        if (since != null) {
            if (desc != null && desc) {
                SQL.append("WHERE LOWER(nickname) < LOWER(?) ");
            } else {
                SQL.append("WHERE LOWER(nickname) > LOWER(?) ");
            }
            args.add(since);
        }

        SQL.append("ORDER BY nickname ");
        if (desc != null) {
            SQL.append((desc ? "DESC" : "ASC"));
        }
        if (limit != null) {
            SQL.append(" LIMIT ? ");
            args.add(limit);
        }

        SQL.append(";");

        return template.query(SQL.toString(), args.toArray(), USER_MAPPER);
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
