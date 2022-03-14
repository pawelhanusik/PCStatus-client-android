package pl.hanusik.pawel.pcstatus;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class TokenManager {
    private SharedPreferences sharedPreferences;

    TokenManager(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getToken() {
        return sharedPreferences.getString("token", "");
    }
    public void setToken(String token) {
        sharedPreferences.edit()
                .putString("token", token)
                .apply();
    }
}
