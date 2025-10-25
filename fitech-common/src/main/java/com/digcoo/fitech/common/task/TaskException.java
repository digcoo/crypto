package com.digcoo.fitech.common.task;

public class TaskException extends RuntimeException {

    private static final long serialVersionUID = -6352615412014554392L;

    public TaskException(Throwable ex) {
        super(ex);
    }
}
