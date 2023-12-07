package cn;

public class Message {

    private Boolean sent;
    private String otherUsername;
    private String content;

    public Message(Boolean sent, String otherUsername, String content){
        this.sent=sent;
        this.otherUsername=otherUsername;
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

    public String getOtherUsername() {
        return otherUsername;
    }

    public void setOtherUsername(String otherUsername) {
        this.otherUsername = otherUsername;
    }
}
