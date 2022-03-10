package pl.hanusik.pawel.pcstatus;

import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProgressFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProgressFragment extends Fragment {
    public static final String ARG_TITLE = "title";
    public static final String ARG_PROGRESS = "progress";
    public static final String ARG_PROGRESS_MAX = "progressMax";
    public static final String ARG_MESSAGE = "message";

    private String title;
    private int progress;
    private int progressMax;
    private String message;

    public ProgressFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @param message Parameter 2.
     * @return A new instance of fragment NotificationFragment.
     */
    public static NotificationFragment newInstance(String title, int progress, int progress_max, String message) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_PROGRESS, progress);
        args.putInt(ARG_PROGRESS_MAX, progress_max);
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.title = getArguments().getString(ARG_TITLE);
            this.progress = getArguments().getInt(ARG_PROGRESS);
            this.progressMax = getArguments().getInt(ARG_PROGRESS_MAX);
            this.message = getArguments().getString(ARG_MESSAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_progress, container, false);

        TextView title = (TextView)v.findViewById(R.id.title);
        title.setText(this.title);

        ProgressBar progressBar = (ProgressBar)v.findViewById(R.id.progressBar);
        progressBar.setMax(this.progressMax);
        ObjectAnimator.ofInt(progressBar, "progress", this.progress)
                .setDuration(1500L * this.progress / this.progressMax)
                .start();

        TextView message = (TextView)v.findViewById(R.id.message);
        message.setText(this.message);
        return v;
    }
}