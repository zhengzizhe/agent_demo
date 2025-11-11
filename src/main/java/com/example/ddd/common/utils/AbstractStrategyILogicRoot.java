package com.example.ddd.common.utils;

public abstract class AbstractStrategyILogicRoot<T, D, R> implements ILogicHandler<T, D, R>, ILogicRouter<T, D, R> {


    protected R router(T t, D d) {
        ILogicHandler<T, D, R> nextHandler = this.getNextHandler();
        if (nextHandler == null) {
            return null;
        }
        return nextHandler.handle(t, d);
    }


}
