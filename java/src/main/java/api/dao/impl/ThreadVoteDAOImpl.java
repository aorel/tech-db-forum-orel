package api.dao.impl;

import api.dao.ThreadVoteDAO;
import api.models.Thread;
import api.models.ThreadVote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@Transactional
public class ThreadVoteDAOImpl implements ThreadVoteDAO {
    private static final ThreadVoteMapper THREAD_VOTE_MAPPER = new ThreadVoteMapper();

    private final JdbcTemplate template;

    @Autowired
    public ThreadVoteDAOImpl(JdbcTemplate template){
        this.template = template;
    }

    @Override
    public ThreadVote get(final Thread thread, final ThreadVote vote){
        final String SQL = "SELECT votes.id AS v_id " +
                "FROM votes " +
                "JOIN users on users.id=votes.user_id " +
                "WHERE LOWER(nickname) = LOWER(?) AND votes.thread_id = ?;";
        return template.queryForObject(SQL, THREAD_VOTE_MAPPER, vote.getNickname(), thread.getId());
    }

    @Override
    public void create(final Thread thread, final ThreadVote vote) {
        final String SQL = "INSERT INTO votes (user_id, thread_id, voice) " +
                "VALUES ((SELECT id FROM users WHERE LOWER(nickname) = LOWER(?)), ?, ?);";
        template.update(SQL, vote.getNickname(), thread.getId(), vote.getVoice());


        final String SQL_UP_THREAD = "UPDATE threads SET __votes = (SELECT SUM(voice) FROM votes " +
                "WHERE (thread_id) = ?) WHERE id = ? RETURNING __votes;";
        int votes =  template.queryForObject(SQL_UP_THREAD, Integer.class, thread.getId(), thread.getId());
        thread.setVotes(votes);
    }

    @Override
    public void insert(final Thread thread, final ThreadVote vote) {
        final String SQL = "UPDATE votes SET voice=? WHERE id=?;";
        template.update(SQL, vote.getVoice(), vote.getId());


        final String SQL_UP_THREAD = "UPDATE threads SET __votes = (SELECT SUM(voice) FROM votes " +
                "WHERE (thread_id) = ?) WHERE id = ? RETURNING __votes;";
        int votes =  template.queryForObject(SQL_UP_THREAD, Integer.class, thread.getId(), thread.getId());
        thread.setVotes(votes);
    }

    private static final class ThreadVoteMapper implements RowMapper<ThreadVote> {
        public ThreadVote mapRow(ResultSet rs, int rowNum) throws SQLException {
            final ThreadVote threadVote = new ThreadVote();
            threadVote.setId(rs.getInt("v_id"));

            return threadVote;
        }
    }
}
