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
}