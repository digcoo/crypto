package com.digcoo.fitech.common.task;

public class BaseTaskCoordWrapper implements ITaskCoord {

    protected final ITaskCoord taskCoord;

    public BaseTaskCoordWrapper(ITaskCoord taskCoord) {
        this.taskCoord = taskCoord;
    }

    @Override
    public void finish(ITask task) {
        taskCoord.finish(task);
    }

    @Override
    public void start() {
        taskCoord.start();
    }

    @Override
    public TaskVoidBatchResult waitResult() throws InterruptedException {
        return taskCoord.waitResult();
    }

}
