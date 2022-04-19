package hcmute.nhom2.zaloapp.model;

public class Contact {
    private String phone;
    private String name;
    private boolean active;
    private String image;

    public Contact() {

    }

    public Contact(String phone, String name, boolean active, String image) {
        this.phone = phone;
        this.name = name;
        this.active = active;
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
