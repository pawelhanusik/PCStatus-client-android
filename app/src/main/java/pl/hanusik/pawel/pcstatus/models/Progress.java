package pl.hanusik.pawel.pcstatus.models;

import org.json.JSONException;
import org.json.JSONObject;

import pl.hanusik.pawel.pcstatus.DateUtils;

public class Progress extends Model {
    public String title;
    public int progress;
    public int progress_max;
    public String message;

    public static String getUrl() {
        return "progress";
    }
    public static Progress fromJson(JSONObject json) {
        try {
            Progress model = new Progress();

            model.id = json.getInt("id");
            model.title = json.getString("title");
            model.progress = json.getInt("progress");
            model.progress_max = json.getInt("progress_max");
            model.message = json.getString("message");
            model.updated_at =  DateUtils.getDateFromString(json.getString("updated_at"));

            return model;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
