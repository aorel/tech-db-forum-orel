package api.dao;

import api.models.Thread;

import java.util.List;

public interface ThreadDAO {
    int create(final Thread thread);

    //List<Thread> getDuplicates(Thread thread);

    List<Thread> get(final String slug, final Integer limit, final String since, final Boolean desc);
}
