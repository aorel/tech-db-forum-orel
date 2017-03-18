package api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ThreadVote {
    private Integer id;
    @JsonProperty
    private String nickname;
    @JsonProperty
    private int voice;

    @JsonCreator
    public ThreadVote(@JsonProperty("nickname") String nickname,
                      @JsonProperty("voice") int voice){
        this.nickname = nickname;
        this.voice = voice;
    }

    public ThreadVote(){
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getVoice() {
        return voice;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }
}
