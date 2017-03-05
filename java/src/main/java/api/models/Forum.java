package api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class Forum {
    @JsonIgnore
    private int id;
    @JsonProperty
    private String title;
    @JsonProperty
    private String user;
    @JsonProperty
    private String slug;
    @JsonProperty
    private int posts;
    @JsonProperty
    private int threads;
    @JsonIgnore
    private int userId;

    @JsonCreator
    public Forum(@JsonProperty("title") String title, @JsonProperty("user") String user,
                 @JsonProperty("slug") String slug, @JsonProperty("posts") int posts,
                 @JsonProperty("threads") int threads) {
        this.title = title;
        this.user = user;
        this.slug = slug;
        this.posts = posts;
        this.threads = threads;
    }

    public Forum(String title, String user, String slug) {
        this.title = title;
        this.user = user;
        this.slug = slug;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public static String toJSON(Forum forum) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(forum);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String toJSON(List<Forum> forums) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(forums);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
