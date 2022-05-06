package pl.hanusik.pawel.pcstatus;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> loginActivityResultLauncher = null;
    private StatusModelsList statusModelsList;

    private Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.loginActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // login successful
                        this.statusModelsList.applyFilter(StatusModelsList.FilterType.ALL);
                    } else {
                        // login failed for some reason
                        startLoginActivity();
                    }
                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setBottomBarButtonsListeners();

        client = new Client(this);
        this.statusModelsList = new StatusModelsList(
                client,
                getSupportFragmentManager(),
                findViewById(R.id.main_scroll_view_ll),
                this.getRefreshIntervalMs()
        );
        this.statusModelsList.setOnFetchErrorCallback((error) -> {
            if (error == Client.Error.UNAUTHENTICATED) {
                startLoginActivity();
            }
        });

        if (savedInstanceState == null) {
            this.statusModelsList.applyFilter(StatusModelsList.FilterType.ALL);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        client.refreshSettings();
    }

    private int getRefreshIntervalMs() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // TODO: store update_refresh_interval already as int
        return Integer.parseInt(
                sharedPreferences.getString("update_refresh_interval", "10")
        ) * 1000;
    }

    private void startLoginActivity() {
        loginActivityResultLauncher.launch(new Intent(this, LoginActivity.class));
    }

    private void setBottomBarButtonsListeners() {
        ImageButton settingsButton = findViewById(R.id.bar_settings);
        settingsButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        Button allButton = findViewById(R.id.bar_all);
        Button notificationsButton = findViewById(R.id.bar_notifications);
        Button progressesButton = findViewById(R.id.bar_progresses);
        Button tasksButton = findViewById(R.id.bar_tasks);

        allButton.setOnClickListener(v -> {
            this.statusModelsList.applyFilter(StatusModelsList.FilterType.ALL);
        });
        notificationsButton.setOnClickListener(v -> {
            this.statusModelsList.applyFilter(StatusModelsList.FilterType.NOTIFICATION);
        });
        progressesButton.setOnClickListener(v -> {
            this.statusModelsList.applyFilter(StatusModelsList.FilterType.PROGRESS);
        });
        tasksButton.setOnClickListener(v -> {
            this.statusModelsList.applyFilter(StatusModelsList.FilterType.TASK);
        });
    }


}