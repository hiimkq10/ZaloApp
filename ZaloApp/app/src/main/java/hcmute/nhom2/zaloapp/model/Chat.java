package hcmute.nhom2.zaloapp.model;

import java.util.Date;

// Lưu thông tin tên, ảnh người nhận, tin nhắn cuối cùng người dùng chat
public class Chat extends Contact {
    private String latestChat; // tin nhắn cuối
    private Date timestamp; // thời gian nhắn
    private Boolean read; // Đã đọc hay chưa

    public Chat() {
    }

    public Chat(String phone, String name, boolean active, String image, String latestChat, Date timestamp, Boolean read) {
        super(phone, name, active, image);
        this.latestChat = latestChat;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getLatestChat() {
        return latestChat;
    }

    public void setLatestChat(String latestChat) {
        this.latestChat = latestChat;
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
}
