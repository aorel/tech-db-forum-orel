package api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class User {
    @JsonIgnore
    private int id;
    @JsonProperty
    private String nickname;
    @JsonProperty
    private String fullname;
    @JsonProperty
    private String about;
    @JsonProperty
    private String email;

    @JsonCreator
    public User(@JsonProperty("nickname") String nickname, @JsonProperty("fullname") String fullname,
                @JsonProperty("about") String about, @JsonProperty("email") String email) {
        this.nickname = nickname;
        this.fullname = fullname;
        this.about = about;
        this.email = email;
    }
    public User(int id, String nickname, String fullname, String about, String email){
        this.id = id;
        this.nickname = nickname;
        this.fullname = fullname;
        this.about = about;
        this.email = email;
    }

    @JsonIgnore
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void __print() {
        System.out.println("[__print]"
                + " nickname:" + nickname
                + " fullname:" + fullname
                + " about:" + about
                + " email:" + email);
    }

    public boolean isNull() {
        return (nickname == null ||
                fullname == null ||
                about == null ||
                email == null);
    }

    public static String toJSON(User user) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(user);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String toJSON(List<User> users) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(users);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
