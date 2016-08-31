package kuik.matthijs.catinder.util;

/**
 * Created by Matthijs Kuik on 7/18/2016.
 */
public class ClientUser {
    private String name;
    private String email;
    private String password;
    private String base64image;

    public ClientUser(String base64image, String email, String name, String password) {
        this.base64image = base64image;
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBase64image() {
        return base64image;
    }

    public void setBase64image(String base64image) {
        this.base64image = base64image;
    }
}
