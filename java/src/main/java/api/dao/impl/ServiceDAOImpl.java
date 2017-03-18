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
        final String SQL = "DELETE FROM users;" +
                "DELETE FROM forums;" +
                "DELETE FROM threads;" +
                "DELETE FROM votes;" +
                "DELETE FROM posts;";
        template.update(SQL);
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
