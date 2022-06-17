package hcmute.nhom2.zaloapp.ConcreteBuilder;

import java.util.Date;

import hcmute.nhom2.zaloapp.builder.ChatBuilder;
import hcmute.nhom2.zaloapp.model.Chat;

// Chat Builder giúp xây dựng đối tượng Class Chat
public class ChatConcreteBuilder implements ChatBuilder {
    private String phone;
    private String name;
    private boolean active;
    private String image;
    private String latestChat;
    private Date timestamp;
    private Boolean read;

    @Override
    public ChatBuilder setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    @Override
    public ChatBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public ChatBuilder setActive(Boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public ChatBuilder setImage(String image) {
        this.image = image;
        return this;
    }

    @Override
    public ChatBuilder setLatestChat(String latestChat) {
        this.latestChat = latestChat;
        return this;
    }

    @Override
    public ChatBuilder setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public ChatBuilder setRead(Boolean read) {
        this.read = read;
        return this;
    }

    // Tạo đối tượng chat
    @Override
    public Chat build() {
        return new Chat(phone, name, active, image, latestChat, timestamp, read);
    }
}
