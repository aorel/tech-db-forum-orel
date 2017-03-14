package api.dao.impl;

import api.dao.PostDAO;
import api.models.Post;
import api.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
public class PostDAOImpl implements PostDAO {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+03:00'");


    @Autowired
    private JdbcTemplate template;

    public void create(Thread thread, List<Post> posts) {
        System.out.println("ThreadDAOImpl create " +
                thread.getSlug() +
                " [" + thread.getId() + "]");
        for (Post post : posts) {

            System.out.println(post.getAuthor());
            Timestamp timestamp;
            if (post.getCreated() != null) {
                System.out.println(post.getCreated());
                timestamp = Timestamp.valueOf(LocalDateTime.parse(post.getCreated(), DateTimeFormatter.ISO_DATE_TIME));
            } else {
                System.out.println("TIME NULL");
                timestamp = Timestamp.valueOf(LocalDateTime.parse(LocalDateTime.now().toString(), DateTimeFormatter.ISO_DATE_TIME));

            }
            System.out.println(post.getMessage());
            System.out.println(post.getParent());

            post.setId(42);
            post.setThread(thread.getId());
            post.setForum(thread.getForum());
            post.setCreated(DATE_FORMAT.format(timestamp));

            System.out.println();
        }
        System.out.println("---------------------------------------------------");
    }
}
