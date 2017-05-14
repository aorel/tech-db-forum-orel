package api.dao.impl;

import api.dao.ServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ServiceDAOImpl implements ServiceDAO {
    @Autowired
    private JdbcTemplate template;

    @Override
    public void clear() {
//        System.out.println("DEL_USERS...");
//        final String DEL_USERS = "DELETE FROM users;";
//        template.update(DEL_USERS);
//
//        System.out.println("DEL_FORUMS...");
//        final String DEL_FORUMS = "DELETE FROM forums;";
//        template.update(DEL_FORUMS);
//
//        System.out.println("DEL_THREADS...");
//        final String DEL_THREADS = "DELETE FROM threads;";
//        template.update(DEL_THREADS);
//
//        System.out.println("DEL_VOTES...");
//        final String DEL_VOTES = "DELETE FROM votes;";
//        template.update(DEL_VOTES);
//
//        System.out.println("DEL_POSTS...");
//        final String DEL_POSTS = "DELETE FROM posts;";
//        template.update(DEL_POSTS);

        final String DEL_ = "TRUNCATE TABLE users, forums, threads, votes, posts CASCADE;";
        template.update(DEL_);
    }

    @Override
    public int getCountUsers() {
        final String SQL = "SELECT count(*) FROM users;";
        return template.queryForObject(SQL, Integer.class);
    }

    @Override
    public int getCountForums() {
        final String SQL = "SELECT count(*) FROM forums;";
        return template.queryForObject(SQL, Integer.class);
    }

    @Override
    public int getCountThreads() {
        final String SQL = "SELECT count(*) FROM threads;";
        return template.queryForObject(SQL, Integer.class);
    }

    @Override
    public int getCountPost() {
        final String SQL = "SELECT count(*) FROM posts;";
        return template.queryForObject(SQL, Integer.class);
    }
}
