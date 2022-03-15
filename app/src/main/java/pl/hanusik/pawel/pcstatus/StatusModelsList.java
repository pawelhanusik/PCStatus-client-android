package pl.hanusik.pawel.pcstatus;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import pl.hanusik.pawel.pcstatus.models.Model;
import pl.hanusik.pawel.pcstatus.models.Notification;
import pl.hanusik.pawel.pcstatus.models.Progress;
import pl.hanusik.pawel.pcstatus.models.Task;

public class StatusModelsList {

    public enum FilterType {
        NONE,
        ALL,
        NOTIFICATION,
        PROGRESS,
        TASK
    }

    private FragmentManager fragmentManager;
    private LinearLayout linearLayout;
    private Client client;
    private FilterType currentlySelectedFilter = FilterType.NONE;

    private Callback<Client.Error> onFetchErrorCallback = error -> {};
    private Callback<Void> onFetchDoneCallback = Void -> {};

    private StatusModelsRepository statusModelsRepository;

    StatusModelsList(Client client, FragmentManager fragmentManager, LinearLayout linearLayout) {
        this.fragmentManager = fragmentManager;
        this.linearLayout = linearLayout;
        this.client = client;

        this.statusModelsRepository = new StatusModelsRepository();

        this.onFetchDoneCallback = Void -> {
            this.updateList();
        };
    }

    public void setOnFetchErrorCallback(Callback<Client.Error> onFetchErrorCallback) {
        this.onFetchErrorCallback = onFetchErrorCallback;
    }

    public void applyFilter(FilterType filterType) {
        if (this.currentlySelectedFilter != filterType) {
            this.currentlySelectedFilter = filterType;

            this.fetch(this.currentlySelectedFilter);
        }
    }

    private void clearList() {
        this.linearLayout.removeAllViews();
    }

    private void updateList() {
        this.statusModelsRepository.commit(this::addModel);

        if (this.statusModelsRepository.size() == 0) {
            this.nothing_to_show();
        }
    }

    private void nothing_to_show() {
        TextView nothingToShowTV = new TextView(this.linearLayout.getContext());
        nothingToShowTV.setText(this.linearLayout.getContext().getString(R.string.nothing_to_show));

        this.linearLayout.addView(nothingToShowTV);
    }

    private void addModel(Model model) {
        if (model instanceof Notification) {
            this.addNotification((Notification) model);
        } else if (model instanceof Progress) {
            this.addProgress((Progress) model);
        } else if (model instanceof Task) {
            this.addTask((Task) model);
        }
    }

    private void addNotification(Notification notification) {
        Bundle args = new Bundle();
        args.putString(NotificationFragment.ARG_TITLE, notification.title);
        args.putString(NotificationFragment.ARG_MESSAGE, notification.message);

        this.fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.main_scroll_view_ll, NotificationFragment.class, args)
                .commit();
    }

    private void addProgress(Progress progress) {
        Bundle args = new Bundle();
        args.putString(ProgressFragment.ARG_TITLE, progress.title);
        args.putInt(ProgressFragment.ARG_PROGRESS, progress.progress);
        args.putInt(ProgressFragment.ARG_PROGRESS_MAX, progress.progress_max);
        args.putString(ProgressFragment.ARG_MESSAGE, progress.message);

        this.fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.main_scroll_view_ll, ProgressFragment.class, args)
                .commit();
    }

    private void addTask(Task task) {
        // TODO:
    }

    // FETCHING

    public void fetch(StatusModelsList.FilterType filterType) {
        clearList();
        this.statusModelsRepository.clear();

        if (filterType == StatusModelsList.FilterType.ALL) {
            fetch_notifications();
            fetch_progresses();
            fetch_tasks();
        } else if (filterType == StatusModelsList.FilterType.NOTIFICATION) {
            fetch_notifications();
        } else if (filterType == StatusModelsList.FilterType.PROGRESS) {
            fetch_progresses();
        } else if (filterType == StatusModelsList.FilterType.TASK) {
            fetch_tasks();
        }
    }

    private void fetch_notifications() {
        this.client.getModelsIndex(
                Model.Type.NOTIFICATION,
                (models) -> {
                    this.statusModelsRepository.batchAdd(models);
                    this.onFetchDoneCallback.onComplete(null);
                },
                this.onFetchErrorCallback
        );
    }

    private void fetch_progresses() {
        this.client.getModelsIndex(
                Model.Type.PROGRESS,
                (models) -> {
                    this.statusModelsRepository.batchAdd(models);
                    this.onFetchDoneCallback.onComplete(null);
                },
                this.onFetchErrorCallback
        );
    }

    private void fetch_tasks() {
        this.client.getModelsIndex(
                Model.Type.TASK,
                (models) -> {
                    this.statusModelsRepository.batchAdd(models);
                    this.onFetchDoneCallback.onComplete(null);
                },
                this.onFetchErrorCallback
        );
    }
}
