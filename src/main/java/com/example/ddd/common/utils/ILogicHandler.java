package com.example.ddd.common.utils;

public interface ILogicHandler<T, D, R> {
    public R handle(T t, D d);
}
