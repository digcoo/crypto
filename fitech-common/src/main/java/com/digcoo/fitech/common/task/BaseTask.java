package com.digcoo.fitech.common.task;

public abstract class BaseTask implements ITask{

    private boolean done;
    private int executeCount;

    @Override
    public void run() {
        try {
            baseRun();
            done = true;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        } finally {
            executeCount++;
        }
    }


    @Override
    public boolean isDone() {
        return done;
    }

    protected abstract void baseRun() throws Exception;

    /**
     * 拿到任务信息
     */
    @Override
    public String getInfo() {
        return getClass().getName();
    }

    /**
     * 重置一些信息,为了重试
     */
    @Override
    public void reset() {
        done = false;
    }


    /**
     * 拿到当前执行次数
     */
    @Override
    public int getExecuteCount() {
        return executeCount;
    }

}
