package api.dao.impl;

import api.dao.UserDAO;
import api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDAOImpl implements UserDAO {
    @Autowired
    private JdbcTemplate template;

    @Override
    public void create(User user) {
        String SQL = "INSERT INTO users (nickname, fullname, email, about) VALUES(?, ?, ?, ?)";
        template.update(SQL, new Object[]{user.getNickname(), user.getFullname(), user.getEmail(), user.getAbout()});
    }

    @Override
    public List<User> getDuplicates(User user) {
        String SQL = "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?) OR LOWER(email) = LOWER(?)";
        return template.query(SQL,
                new Object[]{user.getNickname(), user.getEmail()},
                new UserMapper());
    }

    @Override
    public User getProfile(String nickname) {
        String SQL = "SELECT * FROM users WHERE nickname = ?";
        return template.queryForObject(SQL, new Object[]{nickname}, new UserMapper());
    }

    @Override
    public void setProfile(User updateUser) {
        String SQL = "UPDATE users SET fullname = ?, email = ?, about = ? WHERE nickname = ?";
        int result = template.update(SQL,
                new Object[]{updateUser.getFullname(), updateUser.getEmail(), updateUser.getAbout(), updateUser.getNickname()});
        System.out.println("setProfile result:" + result);
    }

    private static final class UserMapper implements RowMapper<User> {

        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User(rs.getString("nickname"),
                    rs.getString("fullname"),
                    rs.getString("about"),
                    rs.getString("email"));
            return user;
        }
    }
}
