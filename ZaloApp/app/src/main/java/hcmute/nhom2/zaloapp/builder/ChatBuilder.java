package hcmute.nhom2.zaloapp.builder;

import java.util.Date;

import hcmute.nhom2.zaloapp.model.Chat;

public interface ChatBuilder {

    ChatBuilder setPhone(String phone);
    ChatBuilder setName(String name);
    ChatBuilder setActive(Boolean active);
    ChatBuilder setImage(String image);
    ChatBuilder setLatestChat(String latestChat);
    ChatBuilder setTimestamp(Date timestamp);
    ChatBuilder setRead(Boolean read);
    Chat build();
}
