package cn;

import java.util.List;

public class Message {

    private Boolean sent;
    private List<String> otherUsernames;
    private String content;


    public Message(Boolean sent, List<String> otherUsernames, String content){
        this.sent=sent;
        this.otherUsernames=otherUsernames;
        this.content=content;

    }

    public Boolean getSent(){
        return sent;
    }
    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getOtherUsername() {
        return otherUsernames;
    }

    public void setOtherUsername(List<String> otherUsername) {
        this.otherUsernames = otherUsername;
    }
}
