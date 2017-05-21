package api.dao;

import api.models.Thread;
import api.models.ThreadVote;

public interface ThreadVoteDAO {
    ThreadVote get(final Thread thread, final ThreadVote vote);

    void create(final Thread thread, final ThreadVote vote);

    void insert(final Thread thread, final ThreadVote vote);

//    void count(final Thread thread);
}
