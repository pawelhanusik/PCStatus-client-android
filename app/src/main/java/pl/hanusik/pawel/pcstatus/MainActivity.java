package pl.hanusik.pawel.pcstatus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import pl.hanusik.pawel.pcstatus.models.Model;
import pl.hanusik.pawel.pcstatus.models.Notification;
import pl.hanusik.pawel.pcstatus.models.Progress;
import pl.hanusik.pawel.pcstatus.models.Task;

public class MainActivity extends AppCompatActivity {

    private enum FilterType {
        NONE,
        ALL,
        NOTIFICATION,
        PROGRESS,
        TASK
    }

    private Client client;
    private FilterType currentlySelectedFilter = FilterType.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setBottomBarButtonsListeners();

        this.client = new Client(this);

        if (savedInstanceState == null) {
            show_all();
        }
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
            this.show_all();
        });
        notificationsButton.setOnClickListener(v -> {
            this.show_notifications();
        });
        progressesButton.setOnClickListener(v -> {
            this.show_progresses();
        });
        tasksButton.setOnClickListener(v -> {
            this.show_tasks();
        });
    }

    private void show_all() {
        if (this.currentlySelectedFilter == FilterType.ALL) {
            return;
        }
        this.currentlySelectedFilter = FilterType.ALL;
        this.show_clear_all();

        this.fetch_notifications();
        this.fetch_progresses();
        this.fetch_tasks();
    }
    private void show_notifications() {
        if (this.currentlySelectedFilter == FilterType.NOTIFICATION) {
            return;
        }
        this.currentlySelectedFilter = FilterType.NOTIFICATION;
        this.show_clear_all();

        this.fetch_notifications();
    }
    private void show_progresses() {
        if (this.currentlySelectedFilter == FilterType.PROGRESS) {
            return;
        }
        this.currentlySelectedFilter = FilterType.PROGRESS;
        this.show_clear_all();

        this.fetch_progresses();
    }
    private void show_tasks() {
        if (this.currentlySelectedFilter == FilterType.TASK) {
            return;
        }
        this.currentlySelectedFilter = FilterType.TASK;
        this.show_clear_all();

        this.fetch_tasks();
    }
    private void show_clear_all() {
        LinearLayout ll = findViewById(R.id.main_scroll_view_ll);
        ll.removeAllViews();
    }
    private void nothing_to_show() {
        LinearLayout ll = findViewById(R.id.main_scroll_view_ll);

        if (ll.getChildCount() == 0) {
            TextView nothingToShowTV = new TextView(this);
            nothingToShowTV.setText(getString(R.string.nothing_to_show));

            ll.addView(nothingToShowTV);
        }
    }

    private void fetch_notifications() {
        this.client.getModelsIndex(
                Model.Type.NOTIFICATION,
                (models) -> {
                    if (models.size() == 0) {
                        this.nothing_to_show();
                    }
                    for (Model model : models) {
                        if (model != null) {
                            this.addNotification(((Notification)model).title, ((Notification)model).message);
                        }
                    }
                }
        );
    }
    private void fetch_progresses() {
        this.client.getModelsIndex(
                Model.Type.PROGRESS,
                (models) -> {
                    if (models.size() == 0) {
                        this.nothing_to_show();
                    }
                    for (Model model : models) {
                        if (model != null) {
                            this.addProgress(((Progress)model).title, ((Progress)model).progress, ((Progress)model).progress_max, ((Progress)model).message);
                        }
                    }
                }
        );
    }
    private void fetch_tasks() {
        this.client.getModelsIndex(
                Model.Type.TASK,
                (models) -> {
                    if (models.size() == 0) {
                        this.nothing_to_show();
                    }
                    for (Model model : models) {
                        if (model != null) {
                            this.addTask(((Task)model).title, ((Task)model).status, ((Task)model).message);
                        }
                    }
                }
        );
    }

    private void addNotification(String title, String message) {
        Bundle args = new Bundle();
        args.putString(NotificationFragment.ARG_TITLE, title);
        args.putString(NotificationFragment.ARG_MESSAGE, message);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.main_scroll_view_ll, NotificationFragment.class, args)
                .commit();
    }

    private void addProgress(String title, int progress, int progressMax, String message) {
        Bundle args = new Bundle();
        args.putString(ProgressFragment.ARG_TITLE, title);
        args.putInt(ProgressFragment.ARG_PROGRESS, progress);
        args.putInt(ProgressFragment.ARG_PROGRESS_MAX, progressMax);
        args.putString(ProgressFragment.ARG_MESSAGE, message);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.main_scroll_view_ll, ProgressFragment.class, args)
                .commit();
    }

    private void addTask(String title, Task.Status status, String message) {
        // TODO:
    }
}