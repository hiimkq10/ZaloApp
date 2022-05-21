package hcmute.nhom2.zaloapp.model;

public class FriendRequest extends Notification{
    private Contact sender;
    private Contact receiver;

    public FriendRequest() {
    }

    public FriendRequest(String type, Contact sender, Contact receiver) {
        super(type);
        this.sender = sender;
        this.receiver = receiver;
    }

    public Contact getSender() {
        return sender;
    }

    public void setSender(Contact sender) {
        this.sender = sender;
    }

    public Contact getReceiver() {
        return receiver;
    }

    public void setReceiver(Contact receiver) {
        this.receiver = receiver;
    }
}
