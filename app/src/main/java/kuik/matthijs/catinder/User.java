package kuik.matthijs.catinder;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;

public class User {

    private static final String TAG = "User";
    private String name;
    private String email;
    private int id;
    private String image = "";
    private String thumb = "";

    public User(JSONObject json) throws JSONException {
        name = json.getString("name");
        email = json.getString("email");
        id = json.getInt("id");
        image = json.getString("image");
        thumb = json.getString("thumb");
    }

    public User(String name, String email, int id) {
        this.name = name;
        this.email = email;
        this.id = id;
    }

    public String toString() {
        return String.format(Locale.ENGLISH, "id=%d name=%s email=%s", id, name, email);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("email", email);
        json.put("id", id);
        json.put("image", image);
        json.put("thumb", thumb);
        return json;
    }

    public JSONObject toJSONSummary() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("email", email);
        json.put("id", id);
        return json;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getId() {
        return id;
    }
}
