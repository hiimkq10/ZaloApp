package hcmute.nhom2.zaloapp.model;

public class Contact {
    private String name;
    private boolean active;
    private int image;

    public Contact() {

    }

    public Contact(String name, boolean active, int image) {
        this.name = name;
        this.active = active;
        this.image = image;
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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
