package api.dao;

import api.models.Thread;
import api.models.ThreadVote;

public interface ThreadVoteDAO {
    ThreadVote get(Thread thread, ThreadVote vote);

    void create(Thread thread, ThreadVote vote);

    void insert(ThreadVote vote);

    void count(Thread thread);
}
