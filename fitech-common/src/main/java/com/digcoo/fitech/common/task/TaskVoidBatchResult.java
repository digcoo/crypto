package com.digcoo.fitech.common.task;

import java.util.LinkedList;
import java.util.List;

public class TaskVoidBatchResult {

    private final List<ITask> sucesss = new LinkedList<>();
    private final List<ITask> fails = new LinkedList<>();
    private boolean done = true;

    public synchronized void updateResult(ITask task) {
        if (task.isDone()) {
            sucesss.add(task);
        } else {
            fails.add(task);
            done = false;
        }
    }

    public synchronized boolean isDone() {
        return done;
    }

    public synchronized List<ITask> getSucesss() {
        return sucesss;
    }

    public synchronized List<ITask> getFails() {
        return fails;
    }

}
