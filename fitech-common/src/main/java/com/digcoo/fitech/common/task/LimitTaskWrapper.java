package com.digcoo.fitech.common.task;

public class LimitTaskWrapper extends BaseTaskWrapper {

    private final ITaskCoord taskCoord;

    public LimitTaskWrapper(ITask task, LimitTaskCoord taskCoord) {
        super(task);
        this.taskCoord = taskCoord;
    }

    @Override
    public void run() {
        try {
            task.run();
        } finally {
            taskCoord.finish(this);
        }
    }


}
