package com.stephanofer.networkplatform.paper.lifecycle;

/** Coordinates resource cleanup owned by the consumer plugin or installed modules. */
public interface Lifecycle {

    void onShutdown(Runnable action);

    void shutdown();
}
