package pl.hanusik.pawel.pcstatus;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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

    private Callback<Client.Error> onFetchErrorCallback;
    private Callback<Void> onFetchDoneCallback = Void -> {};

    private StatusModelsRepository statusModelsRepository;

    private int runnableRefreshIntervalMs;
    private Runnable refreshRunnable;
    private Handler refreshHandler;

    StatusModelsList(Client client,
                     FragmentManager fragmentManager,
                     LinearLayout linearLayout,
                     int refreshIntervalMs) {

        this.fragmentManager = fragmentManager;
        this.linearLayout = linearLayout;
        this.client = client;

        this.statusModelsRepository = new StatusModelsRepository();
        this.setOnFetchErrorCallback(error -> {});

        this.onFetchDoneCallback = Void -> {
            this.updateList();
        };

        this.runnableRefreshIntervalMs = refreshIntervalMs;
        this.refreshRunnable = () -> {
            fetch(currentlySelectedFilter);
            refreshHandler.postDelayed(refreshRunnable, refreshIntervalMs);
        };
        this.refreshHandler = new Handler();
        this.startUpdateRunnable();
    }

    public void setOnFetchErrorCallback(Callback<Client.Error> onFetchErrorCallback) {
        this.onFetchErrorCallback = error -> {
            if (error == Client.Error.UNAUTHENTICATED) {
                this.stopUpdateRunnable();
            }
            onFetchErrorCallback.onComplete(error);
        };
    }

    public void applyFilter(FilterType filterType) {
        this.startUpdateRunnable();

        if (this.currentlySelectedFilter != filterType) {
            this.currentlySelectedFilter = filterType;

            this.refreshList();
            this.fetch(this.currentlySelectedFilter);
        }
    }

    private void clearList() {
        this.linearLayout.removeAllViews();
    }

    private void refreshList() {
        this.clearList();
        this.statusModelsRepository.foreach(model -> {
            if (this.currentlySelectedFilter == FilterType.ALL) {
                this.addModel(model);
            } else if (
                    this.currentlySelectedFilter == FilterType.NOTIFICATION
                            && model instanceof Notification
            ) {
                this.addModel(model);
            } else if (
                    this.currentlySelectedFilter == FilterType.PROGRESS
                            && model instanceof Progress
            ) {
                this.addModel(model);
            } else if (
                    this.currentlySelectedFilter == FilterType.TASK
                            && model instanceof Task
            ) {
                this.addModel(model);
            }
        });
    }

    private void updateList() {
        this.statusModelsRepository.commit(this::addModel, this::updateModel);
        this.nothing_to_show();
    }

    private void nothing_to_show() {
        if (this.statusModelsRepository.size() == 0) {
            TextView nothingToShowTV = new TextView(this.linearLayout.getContext());
            nothingToShowTV.setText(this.linearLayout.getContext().getString(R.string.nothing_to_show));

            this.linearLayout.addView(nothingToShowTV);
        } else {
            for (int i = 0; i < this.linearLayout.getChildCount(); ++i) {
                View v = this.linearLayout.getChildAt(i);
                if (v instanceof TextView) {
                    this.linearLayout.removeViewAt(i);
                    break;
                }
            }
        }
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

    private void updateModel(Model model) {
        // TODO: implement
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
        // TODO: implement
    }

    // UPDATE RUNNABLE

    private void startUpdateRunnable() {
        this.stopUpdateRunnable();
        this.refreshHandler.postDelayed(refreshRunnable, this.runnableRefreshIntervalMs);
    }

    private void stopUpdateRunnable() {
        this.refreshHandler.removeCallbacks(this.refreshRunnable);
    }

    // FETCHING

    public void fetch(StatusModelsList.FilterType filterType) {
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
                this.onFetchErrorCallback,
                this.statusModelsRepository.getLastUpdateDate()
        );
    }

    private void fetch_progresses() {
        this.client.getModelsIndex(
                Model.Type.PROGRESS,
                (models) -> {
                    this.statusModelsRepository.batchAdd(models);
                    this.onFetchDoneCallback.onComplete(null);
                },
                this.onFetchErrorCallback,
                this.statusModelsRepository.getLastUpdateDate()
        );
    }

    private void fetch_tasks() {
        this.client.getModelsIndex(
                Model.Type.TASK,
                (models) -> {
                    this.statusModelsRepository.batchAdd(models);
                    this.onFetchDoneCallback.onComplete(null);
                },
                this.onFetchErrorCallback,
                this.statusModelsRepository.getLastUpdateDate()
        );
    }
}
