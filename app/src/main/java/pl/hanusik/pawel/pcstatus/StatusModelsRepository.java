package pl.hanusik.pawel.pcstatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pl.hanusik.pawel.pcstatus.models.Model;
import pl.hanusik.pawel.pcstatus.models.Notification;
import pl.hanusik.pawel.pcstatus.models.Progress;
import pl.hanusik.pawel.pcstatus.models.Task;

public class StatusModelsRepository {

    private class Storage {
        private Map<Integer, Notification> notifications;
        private Map<Integer, Progress> progresses;
        private Map<Integer, Task> tasks;

        public Storage() {
            this.notifications = new HashMap<>();
            this.progresses = new HashMap<>();
            this.tasks = new HashMap<>();
        }

        public int size() {
            return this.notifications.size() +
                    this.progresses.size() +
                    this.tasks.size();
        }

        public void clear() {
            this.notifications.clear();
            this.progresses.clear();
            this.tasks.clear();
        }

        public void batchAdd(ArrayList<Model> models) {
            for (Model model : models) {
                this.addOrUpdate(model);
            }
        }

        public void addOrUpdate(Model model) {
            if (model instanceof Notification) {
                this.notifications.put(model.id, (Notification) model);
            } else if (model instanceof Progress) {
                this.progresses.put(model.id, (Progress) model);
            } else if (model instanceof Task) {
                this.tasks.put(model.id, (Task) model);
            }
        }

        public void foreach(Callback<Model> foreachOperation) {
            for (Notification model : this.notifications.values()) {
                foreachOperation.onComplete(model);
            }
            for (Progress model : this.progresses.values()) {
                foreachOperation.onComplete(model);
            }
            for (Task model : this.tasks.values()) {
                foreachOperation.onComplete(model);
            }
        }
    }

    private Storage committed;
    private Storage staged;

    public StatusModelsRepository() {
        this.committed = new Storage();
        this.staged = new Storage();
    }

    public int size() {
        return this.committed.size() + this.staged.size();
    }

    public void clear() {
        this.committed.clear();
        this.staged.clear();
    }

    public void batchAdd(ArrayList<Model> models) {
        this.staged.batchAdd(models);
    }

    public void addOrUpdate(Model model) {
        this.staged.addOrUpdate(model);
    }

    public void commit(Callback<Model> callback) {
        this.staged.foreach(model -> {
            this.committed.addOrUpdate(model);
            callback.onComplete(model);
        });
        this.staged.clear();
    }

    public void foreach(Callback<Model> foreachOperation) {
        this.committed.foreach(foreachOperation);
        this.staged.foreach(foreachOperation);
    }
}
