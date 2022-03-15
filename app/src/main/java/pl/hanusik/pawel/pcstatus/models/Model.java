package pl.hanusik.pawel.pcstatus.models;

import org.json.JSONObject;

import java.util.Date;

public class Model {
    public enum Type {
        NOTIFICATION,
        PROGRESS,
        TASK
    }

    public int id;
    public Date updated_at;

    public static String getUrl() {
        return "";
    }
    public static Model fromJson(JSONObject json) {
        return new Model();
    }
}
