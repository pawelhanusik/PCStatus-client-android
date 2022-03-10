package pl.hanusik.pawel.pcstatus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            this.addNotification("Title 1", "Test message 1\nnew line");
            this.addNotification("Title 2", "Download done!");
            this.addNotification("Title 3", "Everything is all right.");

            this.addProgress("P Title 1", 0, 10, "Test message 1\nnew line");
            this.addProgress("P Title 2", 7, 10, "Download done!");
            this.addProgress("P Title 3", 10, 10, "Everything is all right.");
            this.addProgress("P Title 4", 90, 100, "Everything is all right.");

            for (int i = 0; i < 20; ++i) {
                this.addNotification("Test_" + i, "message");
            }
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