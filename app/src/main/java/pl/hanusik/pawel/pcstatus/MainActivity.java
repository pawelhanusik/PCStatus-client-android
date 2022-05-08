package pl.hanusik.pawel.pcstatus;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> loginActivityResultLauncher = null;
    private boolean loginActivityStarted = false;
    private StatusModelsList statusModelsList;

    private Client client;
    private int updateRefreshInterval = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.loginActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    loginActivityStarted = false;

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

        this.setupBottomBar();

        this.handleSharedPreferences();

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
    public void onPause() {
        super.onPause();
        statusModelsList.stopUpdateRunnable();
    }

    @Override
    public void onResume() {
        super.onResume();
        statusModelsList.startUpdateRunnable();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.main_menu_item_settings:
                this.startSettingsActivity();
                return true;
            case R.id.main_menu_item_logout:
                this.logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        this.changeBottomBarVisibility(
            sharedPreferences.getBoolean("show_filters", false)
        );

        this.setRefreshIntervalMs(sharedPreferences.getString("update_refresh_interval", "10"));

        sharedPreferences.registerOnSharedPreferenceChangeListener((SharedPreferences sharedPreferences1, String key) -> {
            if (key.equals("show_filters")) {
                this.changeBottomBarVisibility(
                    sharedPreferences1.getBoolean("show_filters", false)
                );
            } else if (key.equals("update_refresh_interval")) {
                this.setRefreshIntervalMs(sharedPreferences1.getString("update_refresh_interval", "10"));
                this.statusModelsList.setUpdateRefreshInterval(this.getRefreshIntervalMs());
            }
        });
    }

    private void setRefreshIntervalMs(String newRefreshIntervalMs) {
        this.updateRefreshInterval = Integer.parseInt(newRefreshIntervalMs) * 1000;
    }

    private int getRefreshIntervalMs() {
        return this.updateRefreshInterval;
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void logout() {
        this.client.logout();
        this.startLoginActivity();
    }

    private void startLoginActivity() {
        if (!loginActivityStarted) {
            loginActivityStarted = true;
            loginActivityResultLauncher.launch(new Intent(this, LoginActivity.class));
        }
    }

    private void setupBottomBar() {
        ImageButton settingsButton = findViewById(R.id.bar_settings);
        settingsButton.setOnClickListener((View v) -> this.startSettingsActivity());

        Button allButton = findViewById(R.id.bar_all);
        Button notificationsButton = findViewById(R.id.bar_notifications);
        Button progressesButton = findViewById(R.id.bar_progresses);
        Button tasksButton = findViewById(R.id.bar_tasks);

        allButton.setOnClickListener(v -> this.statusModelsList.applyFilter(StatusModelsList.FilterType.ALL));
        notificationsButton.setOnClickListener(v -> this.statusModelsList.applyFilter(StatusModelsList.FilterType.NOTIFICATION));
        progressesButton.setOnClickListener(v -> this.statusModelsList.applyFilter(StatusModelsList.FilterType.PROGRESS));
        tasksButton.setOnClickListener(v -> this.statusModelsList.applyFilter(StatusModelsList.FilterType.TASK));
    }

    private void changeBottomBarVisibility(boolean visible) {
        ConstraintLayout bottomBarContainer = findViewById(R.id.bottom_bar_container);

        if (visible) {
            bottomBarContainer.setVisibility(View.VISIBLE);
        } else {
            bottomBarContainer.setVisibility(View.GONE);
        }
    }
}