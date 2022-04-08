package hcmute.nhom2.zaloapp.model;

import java.util.Date;

public class Chat {
    private String name;
    private String newestChat;
    private Date timestamp;
    private Boolean read;
    private int image;

    public Chat() {
    }

    public Chat(String name, String newestChat, Date timestamp, Boolean read, int image) {
        this.name = name;
        this.newestChat = newestChat;
        this.timestamp = timestamp;
        this.read = read;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNewestChat() {
        return newestChat;
    }

    public void setNewestChat(String newestChat) {
        this.newestChat = newestChat;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
