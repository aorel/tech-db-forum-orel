package api.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Posts {
    @JsonProperty
    private String marker;

    @JsonProperty
    private List<Post> posts;

    @JsonCreator
    public Posts(@JsonProperty("marker") String marker, @JsonProperty("posts") List<Post> posts){
        this.marker = marker;
        this.posts = posts;
    }

    public Posts(){
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
}
