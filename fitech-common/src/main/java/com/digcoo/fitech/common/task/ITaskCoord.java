package com.digcoo.fitech.common.task;

public interface ITaskCoord {
    void finish(ITask task);

    void start();

    TaskVoidBatchResult waitResult() throws InterruptedException;

}
