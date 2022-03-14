package pl.hanusik.pawel.pcstatus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import pl.hanusik.pawel.pcstatus.models.Model;
import pl.hanusik.pawel.pcstatus.models.Notification;
import pl.hanusik.pawel.pcstatus.models.Progress;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton settingsButton = findViewById(R.id.bar_settings);
        settingsButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        if (savedInstanceState == null) {
            Client client = new Client(this);
            client.getModelsIndex(
                    Model.Type.NOTIFICATION,
                    (models) -> {
                        for (Model model : models) {
                            if (model != null) {
                                this.addNotification(((Notification)model).title, ((Notification)model).message);
                            }
                        }
                    }
            );
            client.getModelsIndex(
                    Model.Type.PROGRESS,
                    (models) -> {
                        for (Model model : models) {
                            if (model != null) {
                                this.addProgress(((Progress)model).title, ((Progress)model).progress, ((Progress)model).progress_max, ((Progress)model).message);
                            }
                        }
                    }
            );
        }
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
}