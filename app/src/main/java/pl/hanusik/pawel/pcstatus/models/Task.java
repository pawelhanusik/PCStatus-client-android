package pl.hanusik.pawel.pcstatus.models;

import org.json.JSONException;
import org.json.JSONObject;

import pl.hanusik.pawel.pcstatus.DateUtils;

public class Task extends Model {
    public enum Status {
        UNKNOWN,
        CREATED,
        STARTED,
        RUNNING,
        DONE
    }

    public String title;
    public Status status;
    public String message;

    public static String getUrl() {
        return "task";
    }
    public static Task fromJson(JSONObject json) {
        try {
            Task model = new Task();

            model.id = json.getInt("id");
            model.title = json.getString("title");
            model.status = Task.stringToStatus(json.getString("status"));
            model.message = json.getString("message");
            model.updated_at =  DateUtils.getDateFromString(json.getString("updated_at"));

            return model;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Status stringToStatus(String statusStr) {
        if (statusStr.equals("created")) {
            return Status.CREATED;
        }
        if (statusStr.equals("started")) {
            return Status.STARTED;
        }
        if (statusStr.equals("running")) {
            return Status.RUNNING;
        }
        if (statusStr.equals("done")) {
            return Status.DONE;
        }
        return Status.UNKNOWN;
    }
}
