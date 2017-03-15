package api.dao.impl;

import api.dao.ThreadVoteDAO;
import api.models.Thread;
import api.models.ThreadVote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class ThreadVoteDAOImpl implements ThreadVoteDAO {
    private static final ThreadVoteMapper THREAD_VOTE_MAPPER = new ThreadVoteMapper();

    @Autowired
    private JdbcTemplate template;

    @Override
    public ThreadVote get(Thread thread, ThreadVote vote){
        final String SQL = "SELECT votes.id AS v_id " +
                "FROM votes " +
                "JOIN users on users.id=votes.user_id " +
                "WHERE LOWER(nickname) = LOWER(?);";
        return template.queryForObject(SQL, THREAD_VOTE_MAPPER, vote.getNickname());
    }

    @Override
    public void create(final Thread thread, final ThreadVote vote) {
        System.out.println("ThreadVoteDAOImpl create");

        System.out.println(thread.getId());
        System.out.println(thread.getTitle());
        System.out.println(thread.getSlug());
        System.out.println(thread.getMessage());
        System.out.println(thread.getForum());
        System.out.println(thread.getAuthor());
        System.out.println(thread.getCreated());

        System.out.println(vote.getNickname());
        System.out.println(vote.getVoice());


        final String SQL = "INSERT INTO votes (user_id, thread_id, voice) " +
                "VALUES ((SELECT id FROM users WHERE LOWER(nickname) = LOWER(?)), ?, ?);";
        template.update(SQL, vote.getNickname(), thread.getId(), vote.getVoice());
    }

    @Override
    public void insert(final ThreadVote vote) {
        System.out.println("ThreadVoteDAOImpl insert");

        final String SQL = "UPDATE votes SET voice=? WHERE id=?;";
        template.update(SQL, vote.getVoice(), vote.getId());
    }

    @Override
    public void count(final Thread thread) {
        final String SQL = "SELECT sum(voice) FROM votes WHERE thread_id=?;";
        int votes = template.queryForObject(SQL, Integer.class, thread.getId());
        thread.setVotes(votes);
    }


    private static final class ThreadVoteMapper implements RowMapper<ThreadVote> {
        public ThreadVote mapRow(ResultSet rs, int rowNum) throws SQLException {
            final ThreadVote threadVote = new ThreadVote();
            threadVote.setId(rs.getInt("v_id"));
            /*threadVote.setUserId(rs.getInt("user_id"));
            threadVote.setThreadId(rs.getInt("thread_id"));

            threadVote.setUserId(rs.getInt("user_id"));*/

            return threadVote;
        }
    }
}
