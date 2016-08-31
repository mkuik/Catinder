package kuik.matthijs.catinder;

import org.json.JSONException;
import org.json.JSONObject;

public class Profile extends User {

    String password;
    boolean connected = false;

    public Profile(JSONObject json) throws JSONException {
        super(json);
        password = json.getString("password");
        connected = json.getBoolean("connected");
    }

    public Profile(String name, String email, int id) {
        super(name, email, id);
    }

    public Profile(String name, String email, int id, String password) {
        super(name, email, id);
        this.password = password;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = super.toJSON();
        jsonObject.put("password", password);
        jsonObject.put("connected", connected);
        return jsonObject;
    }

    @Override
    public JSONObject toJSONSummary() throws JSONException {
        JSONObject jsonObject = super.toJSONSummary();
        jsonObject.put("password", password);
        return jsonObject;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
