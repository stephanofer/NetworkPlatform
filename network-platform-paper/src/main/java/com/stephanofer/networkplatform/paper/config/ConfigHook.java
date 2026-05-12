package com.stephanofer.networkplatform.paper.config;

@FunctionalInterface
public interface ConfigHook {

    void run(ConfigHandle handle);
}
