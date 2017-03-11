package api.dao.impl;

import api.dao.ThreadDAO;
import api.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class ThreadDAOImpl implements ThreadDAO {
    @Autowired
    private JdbcTemplate template;

    @Override
    public int create(Thread thread) {
        /*Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME));
        System.out.println(thread.getTitle() + "\n" +
                thread.getUserId() + "\n" +
                thread.getForumId() + "\n" +
                thread.getMessage() + "\n" +
                timestamp);
//        final String SQL = "INSERT INTO threads (title, user_id, forum_id, messege, created) VALUES (?, ?, ?, ?, ?);";
//        template.update(SQL, new Object[]{thread.getTitle(), thread.getUserId(), thread.getForumId(), thread.getMessage(), timestamp});
        final String SQL = "INSERT INTO threads (title, user_id, forum_id, messege, created) VALUES (?, ?, ?, ?, ?) RETURNING id;";
        int id = template.queryForObject(SQL, new Object[]{thread.getTitle(), thread.getUserId(), thread.getForumId(), thread.getMessage(), timestamp}, Integer.class);
        System.out.println("creadet id:" + id);
        return id;*/

        // если forum и slug совпадают, slug возвращать не надо
        // иначе вернуть оба
        String SQL;
        Object[] object;
        // .isEmpty()
        if(thread.getCreated() == null) {
            SQL = "INSERT INTO threads (title, user_id, forum_id, messege) VALUES (?, ?, ?, ?) RETURNING id;";
            object = new Object[]{thread.getTitle(), thread.getUserId(), thread.getForumId(), thread.getMessage()};
        } else {
            Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME));
            SQL = "INSERT INTO threads (title, user_id, forum_id, messege, created) VALUES (?, ?, ?, ?, ?) RETURNING id;";
            object = new Object[]{thread.getTitle(), thread.getUserId(), thread.getForumId(), thread.getMessage(), timestamp};
        }
        int id = template.queryForObject(SQL, object, Integer.class);
        System.out.println("creadet id:" + id);
        return id;
    }

    @Override
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
    }
}
