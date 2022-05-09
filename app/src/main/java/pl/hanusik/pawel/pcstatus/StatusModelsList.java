package pl.hanusik.pawel.pcstatus;

import android.os.Handler;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentManager;

import java.util.HashMap;
import java.util.Map;

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

    private final FragmentManager fragmentManager;
    private final LinearLayout linearLayout;
    private final Client client;
    private FilterType currentlySelectedFilter = FilterType.NONE;

    private final Map<Integer, ProgressFragment> progressFragmentMap;
    private final Map<Integer, TaskFragment> taskFragmentMap;

    private Callback<Client.Error> onFetchErrorCallback;
    private Callback<Void> onFetchDoneCallback;

    private final StatusModelsRepository statusModelsRepository;

    private int runnableRefreshIntervalMs;
    private Runnable refreshRunnable;
    private Handler refreshHandler;
    private boolean isUpdateRunnableRunning = false;

    StatusModelsList(Client client,
                     FragmentManager fragmentManager,
                     LinearLayout linearLayout,
                     int refreshIntervalMs) {

        this.fragmentManager = fragmentManager;
        this.linearLayout = linearLayout;
        this.client = client;

        this.statusModelsRepository = new StatusModelsRepository();
        this.setOnFetchErrorCallback(error -> {});

        this.progressFragmentMap = new HashMap<>();
        this.taskFragmentMap = new HashMap<>();

        this.onFetchDoneCallback = Void -> this.updateList();

        this.runnableRefreshIntervalMs = refreshIntervalMs;
        this.refreshRunnable = () -> {
            fetch(currentlySelectedFilter);
            refreshHandler.postDelayed(refreshRunnable, this.runnableRefreshIntervalMs);
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

    public void setUpdateRefreshInterval(int newUpdateRefreshInterval) {
        if (this.isUpdateRunnableRunning) {
            this.stopUpdateRunnable();
            this.runnableRefreshIntervalMs = newUpdateRefreshInterval;
            this.startUpdateRunnable();
        } else {
            this.runnableRefreshIntervalMs = newUpdateRefreshInterval;
        }
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
        if (model instanceof Progress) {
            ProgressFragment fragment = this.progressFragmentMap.get(model.id);
            if (fragment != null) {
                fragment.updateProgress(((Progress) model).progress);
            }
        } else if (model instanceof Task) {
            TaskFragment fragment = this.taskFragmentMap.get(model.id);
            if (fragment != null) {
                fragment.updateStatus(((Task) model).status);
            }
        }
    }

    private void addNotification(Notification notification) {
        NotificationFragment fragment = NotificationFragment.newInstance(notification.title, notification.message);

        this.fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.main_scroll_view_ll, fragment)
                .commit();
    }

    private void addProgress(Progress progress) {
        ProgressFragment fragment = ProgressFragment.newInstance(progress.title, progress.progress, progress.progress_max, progress.message);

        this.progressFragmentMap.put(progress.id, fragment);

        this.fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.main_scroll_view_ll, fragment)
                .commit();
    }

    private void addTask(Task task) {
        TaskFragment fragment = TaskFragment.newInstance(task.title, task.status, task.message);

        this.taskFragmentMap.put(task.id, fragment);

        this.fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.main_scroll_view_ll, fragment)
                .commit();
    }

    // UPDATE RUNNABLE

    public void startUpdateRunnable() {
        this.stopUpdateRunnable();
        this.refreshHandler.postDelayed(refreshRunnable, this.runnableRefreshIntervalMs);
        this.isUpdateRunnableRunning = true;
    }

    public void stopUpdateRunnable() {
        this.refreshHandler.removeCallbacks(this.refreshRunnable);
        this.isUpdateRunnableRunning = false;
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
