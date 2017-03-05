package api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Service {
    @JsonProperty
    private int user;
    @JsonProperty
    private int forum;
    @JsonProperty
    private int thread;
    @JsonProperty
    private int post;

    @JsonCreator
    public Service(@JsonProperty("user") int user, @JsonProperty("forum") int forum,
                   @JsonProperty("thread") int thread, @JsonProperty("post") int post) {
        this.user = user;
        this.forum = forum;
        this.thread = thread;
        this.post = post;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getForum() {
        return forum;
    }

    public void setForum(int forum) {
        this.forum = forum;
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

    public int getPost() {
        return post;
    }

    public void setPost(int post) {
        this.post = post;
    }
}
