package com.digcoo.fitech.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;


@Slf4j
public class SerialNoGenerator implements INoGenerator {
    private final long workerId;
    private final long backWardTolerateTime;
    public volatile boolean status;
    private long sequence;
    private static long twepoch = 1435725920677L;
    private static long workerIdBits = 10L;
    private static long maxWorkerId;
    private static long sequenceBits;
    private static long workerIdShift;
    private static long timestampLeftShift;
    private static long sequenceMask;
    private long lastTimestamp;

    public SerialNoGenerator(long workId) {
        this(workId, 0L);
    }

    public SerialNoGenerator(long workId, long backWardTolerateTime) {
        this.status = true;
        this.sequence = 0L;
        this.lastTimestamp = -1L;
        if (workId <= maxWorkerId && workId > 0L) {
            if (backWardTolerateTime < 0L) {
                throw new IllegalArgumentException(String.format("backWardTolerateTime < 0 backWardTolerateTime=%d", backWardTolerateTime));
            } else {
                this.workerId = workId;
                this.backWardTolerateTime = backWardTolerateTime;
                log.info("worker starting. timestamp left shift {}, worker id bits {}, sequence bits {}, workerid {} backWardTolerateTime {}", new Object[]{timestampLeftShift, workerIdBits, sequenceBits, this.workerId, backWardTolerateTime});
            }
        } else {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or <= 0", maxWorkerId));
        }
    }

    public synchronized long nextNo() {
        if (!this.status) {
            log.error("系统已停止,id生成器禁止使用");
            throw new RuntimeException("系统已停止,id生成器禁止使用");
        } else {
            long timestamp = this.timeGen();
            long backWardTime = this.lastTimestamp - timestamp;
            if (backWardTime > this.backWardTolerateTime) {
                long rejectOverTime = this.lastTimestamp - this.backWardTolerateTime;
                log.warn("clock is moving backwards. Rejecting requests until {}. backWardTolerateTime={}", rejectOverTime, this.backWardTolerateTime);
                throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", rejectOverTime - timestamp));
            } else {
                if (backWardTime > 0L) {
                    log.warn("clock is moving backwards. but not Rejecting. backWardTime={} backWardTolerateTime={} timestamp={} lastTimestamp={}", new Object[]{backWardTime, this.backWardTolerateTime, timestamp, this.lastTimestamp});
                    timestamp = this.lastTimestamp;
                }

                if (this.lastTimestamp == timestamp) {
                    this.sequence = this.sequence + 1L & sequenceMask;
                    if (this.sequence == 0L) {
                        timestamp = this.tilNextMillis(this.lastTimestamp);
                    }
                } else {
                    this.sequence = 0L;
                }

                this.lastTimestamp = timestamp;
                return timestamp - twepoch << (int)timestampLeftShift | this.workerId << (int)workerIdShift | this.sequence;
            }
        }
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp;
        for(timestamp = this.timeGen(); timestamp <= lastTimestamp; timestamp = this.timeGen()) {
        }

        return timestamp;
    }

    public static long getTimeById(long id) {
        return (id >> (int)timestampLeftShift) + twepoch;
    }

    public static long getMinIdByTime(Date date) {
        long timestamp = date.getTime();
        return timestamp - twepoch << (int)timestampLeftShift;
    }

    public static long getMaxIdByTime(Date date) {
        long timestamp = date.getTime();
        return timestamp - twepoch << (int)timestampLeftShift | ~(-1L << (int)timestampLeftShift);
    }

    public static long getWorkId(long id) {
        if (id == 0L) {
            return 0L;
        } else {
            long workIdSequenceMask = maxWorkerId << (int)sequenceBits;
            workIdSequenceMask |= sequenceMask;
            long result = id & workIdSequenceMask;
            result >>= (int)sequenceBits;
            return result;
        }
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

    static {
        maxWorkerId = ~(-1L << (int)workerIdBits);
        sequenceBits = 12L;
        workerIdShift = sequenceBits;
        timestampLeftShift = sequenceBits + workerIdBits;
        sequenceMask = ~(-1L << (int)sequenceBits);
    }

}
