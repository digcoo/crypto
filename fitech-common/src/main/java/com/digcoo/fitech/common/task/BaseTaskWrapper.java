package com.digcoo.fitech.common.task;

abstract class BaseTaskWrapper implements ITask {

    protected final ITask task;

    public BaseTaskWrapper(ITask task) {
        this.task = task;
    }

    /**
     * 是否正常完成
     *
     * @return
     */
    @Override
    public boolean isDone() {
        return task.isDone();
    }

    /**
     * 拿到任务信息,主要是为了比较好的记录任务
     */
    @Override
    public String getInfo() {
        return this.getClass().getName();
    }

    /**
     * 重置一些信息,为了重试
     */
    @Override
    public void reset() {
        task.reset();
    }


    /**
     * 拿到当前执行次数
     */
    @Override
    public int getExecuteCount() {
        return task.getExecuteCount();
    }
}
