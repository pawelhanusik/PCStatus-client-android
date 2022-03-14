package pl.hanusik.pawel.pcstatus.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Notification extends Model {
    public String title;
    public String message;

    public static String getUrl() {
        return "notification";
    }
    public static Notification fromJson(JSONObject json) {
        try {
            Notification model = new Notification();

            model.id = json.getInt("id");
            model.title = json.getString("title");
            model.message = json.getString("message");

            return model;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
