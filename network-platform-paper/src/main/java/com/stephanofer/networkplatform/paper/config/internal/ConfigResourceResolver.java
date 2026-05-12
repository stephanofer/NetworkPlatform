package com.stephanofer.networkplatform.paper.config.internal;

import java.io.InputStream;

@FunctionalInterface
public interface ConfigResourceResolver {

    InputStream open(String resourcePath);
}
