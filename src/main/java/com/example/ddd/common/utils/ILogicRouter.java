package com.example.ddd.common.utils;

public interface ILogicRouter<T, D, R> {
    public ILogicHandler<T, D, R> getNextHandler();
}
