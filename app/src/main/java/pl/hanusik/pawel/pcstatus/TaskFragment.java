package pl.hanusik.pawel.pcstatus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import pl.hanusik.pawel.pcstatus.models.Task;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFragment extends Fragment {
    public static final String ARG_TITLE = "title";
    public static final String ARG_STATUS = "status";
    public static final String ARG_MESSAGE = "message";

    private String title;
    private Task.Status status;
    private String message;

    private TextView statusTextView;

    public TaskFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @param message Parameter 2.
     * @return A new instance of fragment TaskFragment.
     */
    public static TaskFragment newInstance(String title, Task.Status status, String message) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_STATUS, status.toString());
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.title = getArguments().getString(ARG_TITLE);
            this.status = Task.Status.valueOf(getArguments().getString(ARG_STATUS));
            this.message = getArguments().getString(ARG_MESSAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task, container, false);
        TextView title = v.findViewById(R.id.title);
        this.statusTextView = v.findViewById(R.id.status);
        TextView message = v.findViewById(R.id.message);
        title.setText(this.title);
        statusTextView.setText(this.status.toString());
        message.setText(this.message);
        return v;
    }

    public void updateStatus(Task.Status newStatus) {
        this.status = newStatus;
        this.statusTextView.setText(this.status.toString());
    }
}