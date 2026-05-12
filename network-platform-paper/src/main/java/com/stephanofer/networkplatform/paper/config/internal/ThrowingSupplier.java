package com.stephanofer.networkplatform.paper.config.internal;

@FunctionalInterface
interface ThrowingSupplier<T> {

    T get() throws Exception;
}
