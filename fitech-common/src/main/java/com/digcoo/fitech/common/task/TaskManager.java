package com.digcoo.fitech.common.task;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.digcoo.fitech.common.task.function.FunctionP0;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhangwenguo on 17/3/23.
 * 负责管理各种基于线程池的任务执行
 */
@Slf4j
public class TaskManager {


    public static final int DEFAULT_GROUP_ID = Integer.MAX_VALUE;

    /**
     * 默认线程队列大小
     */
    public static final int DEFAULT_QUEUE_CAPACITY = Integer.MAX_VALUE;

    /**
     * 当retryCount是-1时候代表无限重试
     */
    public static final int RETRY_COUNT_FOREVER = -1;
    /**
     * 当retryCount是0时候代表不重试
     */
    public static final int RETRY_COUNT_NO = 0;
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
    private final Map<Integer, ExecutorService> executorMap = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduledExecutorService;


    /**
     * 默认的新组线程数量
     */
    private final int defaultGroupThreadCount;

    private final int defaultQueueCapacity;

    private final String nameSuffix;

    public TaskManager(int taskThreadCount, int scheduleThreadCount, int defaultGroupThreadCount) {
        this("", taskThreadCount, scheduleThreadCount, defaultGroupThreadCount, null);
    }

    public TaskManager(String nameSuffix,int taskThreadCount, int scheduleThreadCount, int defaultGroupThreadCount) {
        this(nameSuffix, taskThreadCount, scheduleThreadCount, defaultGroupThreadCount, null);
    }

    public TaskManager(String nameSuffix,int taskThreadCount, int scheduleThreadCount, int defaultGroupThreadCount, Integer queueCapacity) {
        this.nameSuffix = Objects.requireNonNullElse(nameSuffix, "");
        this.defaultQueueCapacity = Objects.requireNonNullElse(queueCapacity, DEFAULT_QUEUE_CAPACITY);
        getOrCreateExecutorServiceByGroup(DEFAULT_GROUP_ID, taskThreadCount);
        AtomicInteger scheduledThreadIndex = new AtomicInteger();
        scheduledExecutorService = new ScheduledThreadPoolExecutor(scheduleThreadCount, r -> {
            Thread thread = new Thread(r);
            thread.setName("TaskManager-scheduledExecutorService-" + nameSuffix + scheduledThreadIndex.incrementAndGet());
            return thread;
        });
        this.defaultGroupThreadCount = defaultGroupThreadCount;
    }

    private ExecutorService getOrCreateExecutorServiceByGroup(int group, int groupThreadCount) {
        return executorMap.computeIfAbsent(group, (key) -> {
            AtomicInteger threadIndex = new AtomicInteger();
            return new ThreadPoolExecutor(groupThreadCount, groupThreadCount,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(defaultQueueCapacity),
                    r -> {
                        Thread thread = new Thread(r);
                        thread.setName("TaskManager-executor-g" + group + "-" + nameSuffix +  threadIndex.incrementAndGet());
                        return thread;
                    });
        });
    }

    private ExecutorService getDefaultExecutor() {
        return executorMap.get(DEFAULT_GROUP_ID);
    }

    public void stop() {
        for (ExecutorService executorService : executorMap.values()) {
            executorService.shutdown();
        }
        scheduledExecutorService.shutdown();
    }


    public void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
    }


    public void schedule(Runnable command, long delay, TimeUnit unit) {
        scheduledExecutorService.schedule(command, delay, unit);
    }

    /**
     * 按照timeInterval返回的时间，执行任务
     *
     * @param task
     * @param timeInterval
     * @param unit
     */
    public void executeAtConfigRate(int group, int groupThreadCount, FunctionP0<TaskRateStatus> task, FunctionP0<Long> timeInterval, TimeUnit unit) {
        ExecutorService executor = getOrCreateExecutorServiceByGroup(group, groupThreadCount);
        IntervalConfigRateRunner runner = new IntervalConfigRateRunner(executor, task, timeInterval, unit);
        executor.execute(runner);
    }

    public void executeAtConfigRate(int group, int groupThreadCount, FunctionP0<TaskRateStatus> task, long interval, TimeUnit unit) {
        executeAtConfigRate(group, groupThreadCount, task, () -> {
            return interval;
        }, unit);
    }

    public void executeAtConfigRate(FunctionP0<TaskRateStatus> task, FunctionP0<Long> timeInterval, TimeUnit unit) {
        executeAtConfigRate(DEFAULT_GROUP_ID, defaultGroupThreadCount, task, timeInterval, unit);
    }

    public void executeAtConfigRate(int group, FunctionP0<TaskRateStatus> task, FunctionP0<Long> timeInterval, TimeUnit unit) {
        executeAtConfigRate(group, defaultGroupThreadCount, task, timeInterval, unit);
    }

    public void executeAtConfigRate(int group, Runnable task, FunctionP0<Long> timeInterval, TimeUnit unit) {
        FunctionP0<TaskRateStatus> invoker = () -> {
            task.run();
            return TaskRateStatus.Continue;
        };
        executeAtConfigRate(group, defaultGroupThreadCount, invoker, timeInterval, unit);
    }

    public void executeAtConfigRate(Runnable task, FunctionP0<Long> timeInterval, TimeUnit unit) {
        FunctionP0<TaskRateStatus> invoker = () -> {
            task.run();
            return TaskRateStatus.Continue;
        };
        executeAtConfigRate(DEFAULT_GROUP_ID, defaultGroupThreadCount, invoker, timeInterval, unit);
    }

    public void executeAtRate(FunctionP0<TaskRateStatus> task, long interval, TimeUnit unit) {
        executeAtConfigRate(DEFAULT_GROUP_ID, defaultGroupThreadCount, task, () -> {
            return interval;
        }, unit);
    }

    public void executeAtRate(int group, Runnable task, long interval, TimeUnit unit) {
        FunctionP0<TaskRateStatus> invoker = () -> {
            task.run();
            return TaskRateStatus.Continue;
        };
        executeAtConfigRate(group, defaultGroupThreadCount, invoker, () -> {
            return interval;
        }, unit);
    }

    public void executeAtRate(Runnable task, long interval, TimeUnit unit) {
        FunctionP0<TaskRateStatus> invoker = () -> {
            task.run();
            return TaskRateStatus.Continue;
        };
        executeAtConfigRate(DEFAULT_GROUP_ID, defaultGroupThreadCount, invoker, () -> {
            return interval;
        }, unit);
    }

    public TaskVoidBatchResult executeBatchAndWait(Collection<ITask> batchs, int conc) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new LimitTaskCoord(batchs, conc, executor, new TaskVoidBatchResult());
        taskCoord.start();
        try {
            return taskCoord.waitResult();
        } catch (InterruptedException ex) {
            throw new TaskException(ex);
        }
    }

    public TaskVoidBatchResult executeBatchAndWait(Collection<ITask> batchs, int conc, int retryCount) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new RetryTaskCoordWrapper(new LimitTaskCoord(batchs, conc, executor, new TaskVoidBatchResult()), retryCount, executor);
        taskCoord.start();
        try {
            return taskCoord.waitResult();
        } catch (InterruptedException ex) {
            throw new TaskException(ex);
        }
    }

    public TaskBatchResult executeBatchAndWaitForResult(Collection<ITask> batchs, int conc, int retryCount) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new RetryTaskCoordWrapper(new LimitTaskCoord(batchs, conc, executor, new TaskBatchResult()), retryCount, executor);
        taskCoord.start();
        try {
            return (TaskBatchResult) taskCoord.waitResult();
        } catch (InterruptedException ex) {
            throw new TaskException(ex);
        }
    }

    public TaskBatchResult executeBatchAndWaitForResult(Collection<ITask> batchs, int conc) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new LimitTaskCoord(batchs, conc, executor, new TaskBatchResult());
        taskCoord.start();
        try {
            return (TaskBatchResult) taskCoord.waitResult();
        } catch (InterruptedException ex) {
            throw new TaskException(ex);
        }
    }

    public TaskVoidBatchResult executeBatchAndWait(int conc, ITask... tasks) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new LimitTaskCoord(tasks, conc, executor, new TaskVoidBatchResult());
        taskCoord.start();
        try {
            return taskCoord.waitResult();
        } catch (InterruptedException ex) {
            throw new TaskException(ex);
        }
    }

    public TaskVoidBatchResult executeBatchAndWait(ITask... tasks) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new LimitTaskCoord(tasks, tasks.length, executor, new TaskVoidBatchResult());
        taskCoord.start();
        try {
            return taskCoord.waitResult();
        } catch (InterruptedException ex) {
            throw new TaskException(ex);
        }
    }

    public TaskVoidBatchResult executeBatchAndWait(int conc, int retryCount, ITask... tasks) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new RetryTaskCoordWrapper(new LimitTaskCoord(tasks, conc, executor, new TaskVoidBatchResult()), retryCount, executor);
        taskCoord.start();
        try {
            return taskCoord.waitResult();
        } catch (InterruptedException ex) {
            throw new TaskException(ex);
        }
    }

    public void executeBatch(Collection<ITask> batchs, int conc) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new LimitTaskCoord(batchs, conc, executor, new TaskVoidBatchResult());
        taskCoord.start();
    }

    public void executeBatch(Collection<ITask> batchs, int conc, int retryCount) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new RetryTaskCoordWrapper(new LimitTaskCoord(batchs, conc, executor, new TaskVoidBatchResult()), retryCount, executor);
        taskCoord.start();
    }

    public void executeBatch(int conc, ITask... tasks) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new LimitTaskCoord(tasks, conc, executor, new TaskVoidBatchResult());
        taskCoord.start();
    }

    public void executeBatch(ITask... tasks) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new LimitTaskCoord(tasks, tasks.length, executor, new TaskVoidBatchResult());
        taskCoord.start();
    }

    public void executeBatch(int conc, int retryCount, ITask... tasks) {
        ExecutorService executor = getDefaultExecutor();
        ITaskCoord taskCoord = new RetryTaskCoordWrapper(new LimitTaskCoord(tasks, conc, executor, new TaskVoidBatchResult()), retryCount, executor);
        taskCoord.start();
    }

    public <T1> TaskBatchResult<JSONObject, T1> forEachSubmitBatchAndWait(JSONArray items, ITaskParamHandler<T1, JSONObject> handler, int conc) {
        return forEachSubmitBatchAndWait(items, handler, conc, RETRY_COUNT_NO);
    }

    public <T1> TaskBatchResult<JSONObject, T1> forEachSubmitBatchAndWait(JSONArray items, ITaskParamHandler<T1, JSONObject> handler, int conc, int retryCount) {
        JSONObject[] jsonObjects = new JSONObject[items.size()];
        int i = 0;
        for (Object jsonObject : items) {
            jsonObjects[i] = (JSONObject) jsonObject;
            i++;
        }
        return forEachSubmitBatchAndWait(jsonObjects, handler, conc, retryCount);
    }

    public <T1, T2> TaskBatchResult<T2, T1> forEachSubmitBatchAndWait(Collection<T2> items, ITaskParamHandler<T1, T2> handler, int conc) {
        return forEachSubmitBatchAndWait(items, handler, conc, RETRY_COUNT_NO);
    }

    public <T1, T2> TaskBatchResult<T2, T1> forEachSubmitBatchAndWait(Collection<T2> items, ITaskParamHandler<T1, T2> handler) {
        int conc = items.size();
        return forEachSubmitBatchAndWait(items, handler, conc, RETRY_COUNT_NO);
    }

    public <T1, T2> TaskBatchResult<T2, T1> forEachSubmitBatchAndWait(Collection<T2> items, ITaskParamHandler<T1, T2> handler, int conc, int retryCount) {
        return forEachSubmitBatchAndWait((T2[]) items.toArray(), handler, conc, retryCount);
    }

    public <T1, T2> TaskBatchResult<T2, T1> forEachSubmitBatchAndWait(T2[] items, ITaskParamHandler<T1, T2> handler, int conc) {
        return forEachSubmitBatchAndWait(items, handler, conc, RETRY_COUNT_NO);
    }

    public <T1, T2> TaskBatchResult<T2, T1> forEachSubmitBatchAndWait(T2[] items, ITaskParamHandler<T1, T2> handler, int conc, int retryCount) {
        List<ITask> taskList = new LinkedList<>();
        ConcurrentMap<Integer, T1> posResultMap = new ConcurrentHashMap<>();
        ConcurrentMap<T2, T1> itemResultMap = new ConcurrentHashMap<>();
        int index = 0;
        for (T2 item : items) {
            int finalIndex = index;
            ITask task = new BaseTask() {
                @Override
                protected void baseRun() throws Exception {
                    T1 result = handler.handle(item);
                    posResultMap.put(finalIndex, result);
                    itemResultMap.put(item, result);
                }
            };
            index++;
            taskList.add(task);
        }
        TaskBatchResult taskBatchResult;
        if (retryCount != RETRY_COUNT_NO) {
            taskBatchResult = executeBatchAndWaitForResult(taskList, conc, retryCount);
        } else {
            taskBatchResult = executeBatchAndWaitForResult(taskList, conc);
        }
        taskBatchResult.setPosResultMap(posResultMap);
        taskBatchResult.setItemResultMap(itemResultMap);
        return taskBatchResult;
    }

    public <T> TaskBatchResult<Void, T> submitBatchAndWait(int conc, ITaskHandler<T>... handlers) {
        return submitBatchAndWait(conc, RETRY_COUNT_NO, handlers);
    }

    public <T> TaskBatchResult<Void, T> submitBatchAndWait(int conc, int retryCount, ITaskHandler<T>... handlers) {
        List<ITask> taskList = new LinkedList<>();
        ConcurrentMap<Integer, T> posResultMap = new ConcurrentHashMap<>();
        int index = 0;
        for (ITaskHandler<T> handler : handlers) {
            int finalIndex = index;
            ITask task = new BaseTask() {
                @Override
                protected void baseRun() throws Exception {
                    T result = handler.handle();
                    posResultMap.put(finalIndex, result);
                }
            };
            index++;
            taskList.add(task);
        }
        TaskBatchResult taskBatchResult;
        if (retryCount != RETRY_COUNT_NO) {
            taskBatchResult = executeBatchAndWaitForResult(taskList, conc, retryCount);
        } else {
            taskBatchResult = executeBatchAndWaitForResult(taskList, conc);
        }
        taskBatchResult.setPosResultMap(posResultMap);
        return taskBatchResult;
    }

    public <T> TaskVoidBatchResult forEachSubmitBatchAndWait(JSONArray items, IVoidParamHandler<JSONObject> handler, int conc) {
        return forEachSubmitBatchAndWait(items, handler, conc, RETRY_COUNT_NO);
    }

    public <T> TaskVoidBatchResult forEachSubmitBatchAndWait(JSONArray items, IVoidParamHandler<JSONObject> handler, int conc, int retryCount) {
        JSONObject[] jsonObjects = new JSONObject[items.size()];
        int i = 0;
        for (Object jsonObject : items) {
            jsonObjects[i] = (JSONObject) jsonObject;
            i++;
        }
        return forEachSubmitBatchAndWait(jsonObjects, handler, conc, retryCount);
    }

    public <T> TaskVoidBatchResult forEachSubmitBatchAndWait(Collection<T> items, IVoidParamHandler<T> handler, int conc) {
        return forEachSubmitBatchAndWait(items, handler, conc, RETRY_COUNT_NO);
    }

    public <T> TaskVoidBatchResult forEachSubmitBatchAndWait(Collection<T> items, IVoidParamHandler<T> handler) {
        int conc = items.size();
        return forEachSubmitBatchAndWait(items, handler, conc, RETRY_COUNT_NO);
    }

    public <T> TaskVoidBatchResult forEachSubmitBatchAndWait(Collection<T> items, IVoidParamHandler<T> handler, int conc, int retryCount) {
        return forEachSubmitBatchAndWait((T[]) items.toArray(), handler, conc, retryCount);
    }

    public <T> TaskVoidBatchResult forEachSubmitBatchAndWait(T[] items, IVoidParamHandler<T> handler, int conc) {
        return forEachSubmitBatchAndWait(items, handler, conc, RETRY_COUNT_NO);
    }

    public <T> TaskVoidBatchResult forEachSubmitBatchAndWait(T[] items, IVoidParamHandler<T> handler, int conc, int retryCount) {
        List<ITask> taskList = new LinkedList<>();
        for (T item : items) {
            ITask task = new BaseTask() {
                @Override
                protected void baseRun() throws Exception {
                    handler.handle(item);
                }
            };
            taskList.add(task);
        }
        TaskVoidBatchResult taskBatchResult;
        if (retryCount != RETRY_COUNT_NO) {
            taskBatchResult = executeBatchAndWait(taskList, conc, retryCount);
        } else {
            taskBatchResult = executeBatchAndWait(taskList, conc);
        }
        return taskBatchResult;
    }

    private class IntervalConfigRateRunner implements Runnable {

        private final ExecutorService executor;

        private final FunctionP0<TaskRateStatus> task;

        private final FunctionP0<Long> timeInterval;

        private final TimeUnit timeUnit;

        public IntervalConfigRateRunner(ExecutorService executor, FunctionP0<TaskRateStatus> task, FunctionP0<Long> timeInterval, TimeUnit timeUnit) {
            this.executor = executor;
            this.task = task;
            this.timeInterval = timeInterval;
            this.timeUnit = timeUnit;
        }

        @Override
        public void run() {
            TaskRateStatus taskRateStatus = TaskRateStatus.Continue;
            try {
                taskRateStatus = task.run();
            } catch (Exception ex) {
                logger.error("task execute error", ex);
            } finally {
                if (taskRateStatus == TaskRateStatus.Stop) {
                    //什么也不做，停止
                } else if (taskRateStatus == TaskRateStatus.Continue) {
                    //按照时间继续
                    schedule(() -> {
                        executor.execute(this);
                    }, timeInterval.run(), timeUnit);
                } else if (taskRateStatus == TaskRateStatus.Instant) {
                    //立即进行
                    schedule(() -> {
                        executor.execute(this);
                    }, 0, timeUnit);
                } else if (taskRateStatus == TaskRateStatus.WaitTime5Multiple) {
                    //等待5倍的间隔
                    schedule(() -> {
                        executor.execute(this);
                    }, timeInterval.run() * 5, timeUnit);
                } else if (taskRateStatus == TaskRateStatus.WaitTime10Multiple) {
                    //等待10倍的间隔
                    schedule(() -> {
                        executor.execute(this);
                    }, timeInterval.run() * 10, timeUnit);
                } else if (taskRateStatus == TaskRateStatus.Wait1Second) {
                    //等待1秒钟
                    schedule(() -> {
                        executor.execute(this);
                    }, 1 * 1000, timeUnit);
                } else if (taskRateStatus == TaskRateStatus.Wait5Second) {
                    //等待5秒钟
                    schedule(() -> {
                        executor.execute(this);
                    }, 5 * 1000, timeUnit);
                } else if (taskRateStatus == TaskRateStatus.Wait10Second) {
                    //等待10秒钟
                    schedule(() -> {
                        executor.execute(this);
                    }, 10 * 1000, timeUnit);
                }
            }
        }
    }
}
