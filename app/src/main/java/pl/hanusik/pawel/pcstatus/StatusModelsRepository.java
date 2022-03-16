package pl.hanusik.pawel.pcstatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
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
            this.notifications = new LinkedHashMap<>();
            this.progresses = new LinkedHashMap<>();
            this.tasks = new LinkedHashMap<>();
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

        public boolean addOrUpdate(Model model) {
            if (model instanceof Notification) {
                return (this.notifications.put(model.id, (Notification) model) == null);
            }
            if (model instanceof Progress) {
                return (this.progresses.put(model.id, (Progress) model) == null);
            }
            if (model instanceof Task) {
                return (this.tasks.put(model.id, (Task) model) == null);
            }

            return true;
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
    private Date lastUpdateDate;

    public StatusModelsRepository() {
        this.committed = new Storage();
        this.staged = new Storage();
        this.lastUpdateDate = DateUtils.minusDays(
                DateUtils.getCurrentDate(),
                1
        );
    }

    public int size() {
        return this.committed.size() + this.staged.size();
    }

    public void clear() {
        this.committed.clear();
        this.staged.clear();
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void batchAdd(ArrayList<Model> models) {
        for (Model model : models) {
            this.lastUpdateDate = DateUtils.getNewer(this.lastUpdateDate, model.updated_at);
        }
        this.staged.batchAdd(models);
    }

    public void addOrUpdate(Model model) {
        this.lastUpdateDate = DateUtils.getNewer(this.lastUpdateDate, model.updated_at);
        this.staged.addOrUpdate(model);
    }

    public void commit(Callback<Model> onAdd, Callback<Model> onUpdate) {
        this.staged.foreach(model -> {
            boolean isValueNew = this.committed.addOrUpdate(model);
            if (isValueNew) {
                onAdd.onComplete(model);
            } else {
                onUpdate.onComplete(model);
            }
        });
        this.staged.clear();
    }

    public void foreach(Callback<Model> foreachOperation) {
        this.committed.foreach(foreachOperation);
        this.staged.foreach(foreachOperation);
    }
}
