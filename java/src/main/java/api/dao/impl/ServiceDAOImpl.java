package api.dao.impl;

import api.dao.ServiceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ServiceDAOImpl implements ServiceDAO {
    private final JdbcTemplate template;

    @Autowired
    public ServiceDAOImpl(JdbcTemplate template){
        this.template = template;
    }

    @Override
    public void clear() {
        final String DEL_ = "TRUNCATE TABLE users, forums, threads, votes, posts CASCADE;";
        template.execute(DEL_);
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
