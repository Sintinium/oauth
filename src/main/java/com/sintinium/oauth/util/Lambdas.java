package com.sintinium.oauth.util;

public class Lambdas {
    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface FunctionWithException<T, R> {
        R apply(T t) throws Exception;
    }

    public interface RunnableWithException {
        void run() throws Exception;
    }
}
