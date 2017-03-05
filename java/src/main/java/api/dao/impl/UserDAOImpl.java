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
    @Autowired
    private JdbcTemplate template;

    @Override
    public void create(User user) {
        final String SQL = "INSERT INTO users (nickname, fullname, email, about) VALUES(?, ?, ?, ?)";
        template.update(SQL, new Object[]{user.getNickname(), user.getFullname(), user.getEmail(), user.getAbout()});
    }

    @Override
    public List<User> getDuplicates(User user) {
        final String SQL = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?) OR LOWER(email) = LOWER(?)";
        return template.query(SQL,
                new Object[]{user.getNickname(), user.getEmail()},
                new UserMapper());
    }

    @Override
    public User getProfile(String nickname) {
        final String SQL = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)";
        return template.queryForObject(SQL, new Object[]{nickname}, new UserMapper());
    }

    @Override
    public void setProfile(User user) {
        /*final String SQL = "UPDATE users SET fullname = ?, email = ?, about = ? WHERE nickname = ?";
        int result = template.update(SQL,
                new Object[]{user.getFullname(), user.getEmail(), user.getAbout(), user.getNickname()});
        System.out.println("setProfile result:" + result);*/

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

    private static final class UserMapper implements RowMapper<User> {
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            final User user = new User(rs.getInt("id"),
                    rs.getString("nickname"),
                    rs.getString("fullname"),
                    rs.getString("about"),
                    rs.getString("email"));
            return user;
        }
    }
}
