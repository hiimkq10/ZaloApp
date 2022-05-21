package hcmute.nhom2.zaloapp.model;

public class Notification {
    private String id;
    private String Type;

    public Notification() {
    }

    public Notification(String type) {
        Type = type;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
