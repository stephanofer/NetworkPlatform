package com.stephanofer.networkplatform.paper.config;

public interface TypedConfigHandle<T> extends ConfigHandle {

    Class<T> type();

    String snapshotPath();

    T snapshot();

    T refreshSnapshot();
}
