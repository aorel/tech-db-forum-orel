package api.dao;

import api.models.Thread;

import java.util.List;

public interface ThreadDAO {
    int create(Thread thread);

    List<Thread> getDuplicates(Thread thread);
}
