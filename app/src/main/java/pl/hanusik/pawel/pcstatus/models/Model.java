package pl.hanusik.pawel.pcstatus.models;

import org.json.JSONObject;

public class Model {
    public enum Type {
        NOTIFICATION,
        PROGRESS,
        TASK
    }

    public int id;

    public static String getUrl() {
        return "";
    }
    public static Model fromJson(JSONObject json) {
        return new Model();
    }
}
