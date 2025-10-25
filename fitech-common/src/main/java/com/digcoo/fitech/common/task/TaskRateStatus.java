package com.digcoo.fitech.common.task;

public enum TaskRateStatus {
    Stop,
    Continue,
    Instant,
    WaitTime5Multiple,
    WaitTime10Multiple,
    Wait1Second,
    Wait5Second,
    Wait10Second;

    private TaskRateStatus() {
    }
}
