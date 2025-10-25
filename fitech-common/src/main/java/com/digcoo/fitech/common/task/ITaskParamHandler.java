package com.digcoo.fitech.common.task;

public interface ITaskParamHandler<T1, T2> {
    T1 handle(T2 t2) throws Exception;
}
