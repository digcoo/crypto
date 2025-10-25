package com.digcoo.fitech.common.task;

public interface ITask extends Runnable{
    /**
     * 是否正常完成
     *
     * @return
     */
    boolean isDone();


    /**
     * 拿到任务信息,主要是为了比较好的记录任务
     */
    String getInfo();


    /**
     * 重置一些信息,为了重试
     */
    void reset();


    /**
     * 拿到当前执行次数
     */
    int getExecuteCount();
}
