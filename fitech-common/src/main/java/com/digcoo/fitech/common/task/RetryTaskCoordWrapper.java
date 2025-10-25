package com.digcoo.fitech.common.task;

import java.util.concurrent.Executor;

public class RetryTaskCoordWrapper extends BaseTaskCoordWrapper{

    private final int retryCount;

    private final Executor executor;

    public RetryTaskCoordWrapper(ITaskCoord taskCoord, int retryCount, Executor executor) {
        super(taskCoord);
        this.retryCount = retryCount;
        this.executor = executor;
    }

    @Override
    public void finish(ITask task) {
        if (task.isDone()) {
            taskCoord.finish(task);
        } else {
            if (retryCount != TaskManager.RETRY_COUNT_FOREVER && task.getExecuteCount() - 1 > retryCount) {
                taskCoord.finish(task);
            } else {
                task.reset();
                executor.execute(task);
            }
        }
    }
}
