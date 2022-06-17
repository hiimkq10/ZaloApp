package hcmute.nhom2.zaloapp.model;

import java.util.Date;

// Tin nhắn
public class ChatMessage {
    private String senderPhoneNum; // Số điện thoại người gửi
    private String type; // Loại tin nhắn, có 2 loại text, image
    private String content; // Nội dung tin nhắn
    private Date timestamp; // Thời gian gửi

    public ChatMessage() {
    }

    public String getSenderPhoneNum() {
        return senderPhoneNum;
    }

    public void setSenderPhoneNum(String senderPhoneNum) {
        this.senderPhoneNum = senderPhoneNum;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
