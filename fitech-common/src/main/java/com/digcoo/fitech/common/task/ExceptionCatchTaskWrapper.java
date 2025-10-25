package com.digcoo.fitech.common.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionCatchTaskWrapper extends BaseTaskWrapper {

    private static Logger logger = LoggerFactory.getLogger(ExceptionCatchTaskWrapper.class);

    public ExceptionCatchTaskWrapper(ITask task) {
        super(task);
    }

    @Override
    public void run() {
        try {
            task.run();
        } catch (Throwable ex) {
            logger.error("catch task exception info=" + task.getInfo(), ex);
        }
    }
}
