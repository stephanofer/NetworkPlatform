package com.stephanofer.networkplatform.paper.config;

@FunctionalInterface
public interface ConfigValueMapper {

    Object map(ConfigDocument document, String route, Object currentValue);
}
