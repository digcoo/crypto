package com.digcoo.fitech.common.task;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class TaskBatchResult<K, T> extends TaskVoidBatchResult {

    private ConcurrentMap<K, T> itemResultMap;

    private ConcurrentMap<Integer, T> posResultMap;

    public Map<K, T> getItemResultMap() {
        return itemResultMap;
    }

    protected void setItemResultMap(ConcurrentMap<K, T> itemResultMap) {
        this.itemResultMap = itemResultMap;
    }

    public Map<Integer, T> getPosResultMap() {
        return posResultMap;
    }

    protected void setPosResultMap(ConcurrentMap<Integer, T> posResultMap) {
        this.posResultMap = posResultMap;
    }

}
