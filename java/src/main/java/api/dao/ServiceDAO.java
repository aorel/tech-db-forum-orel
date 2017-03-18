package api.dao;

public interface ServiceDAO {
    void clear();

    int getCountUsers();

    int getCountForums();

    int getCountThreads();

    int getCountPost();
}
