package com.digcoo.fitech.common.task;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

public class LimitTaskCoord implements ITaskCoord{

    private final Queue<ITask> queue = new LinkedBlockingQueue<>();

    private final int conc;

    private final Executor executor;

    private final CountDownLatch countDownLatch;

    private final TaskVoidBatchResult batchResult;

    public LimitTaskCoord(Collection<ITask> tasks, int conc, Executor executor, TaskVoidBatchResult batchResult) {
        this.conc = conc;
        this.executor = executor;
        countDownLatch = new CountDownLatch(tasks.size());
        this.batchResult = batchResult;
        for (ITask runnable : tasks) {
            queue.add(runnable);
        }
    }

    public LimitTaskCoord(ITask[] tasks, int conc, Executor executor, TaskVoidBatchResult batchResult) {
        this.conc = conc;
        this.executor = executor;
        countDownLatch = new CountDownLatch(tasks.length);
        this.batchResult = batchResult;
        for (ITask runnable : tasks) {
            queue.add(runnable);
        }
    }


    @Override
    public void finish(ITask finishTask) {
        batchResult.updateResult(finishTask);
        ITask newTask = queue.poll();
        if (newTask != null) {
            executor.execute(wrapperTask(newTask));
        }
        countDownLatch.countDown();
    }


    private ITask wrapperTask(ITask task) {
        return new LimitTaskWrapper(new ExceptionCatchTaskWrapper(task), this);
    }


    @Override
    public void start() {
        for (int i = 0; i < conc; i++) {
            ITask task = queue.poll();
            if (task == null) {
                break;
            }
            executor.execute(wrapperTask(task));
        }
    }

    @Override
    public TaskVoidBatchResult waitResult() throws InterruptedException {
        countDownLatch.await();
        return batchResult;
    }

}
