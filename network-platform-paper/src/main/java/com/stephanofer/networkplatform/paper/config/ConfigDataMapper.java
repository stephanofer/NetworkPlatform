package com.stephanofer.networkplatform.paper.config;

@FunctionalInterface
public interface ConfigDataMapper<T> {

    T map(ConfigDocument document);
}
