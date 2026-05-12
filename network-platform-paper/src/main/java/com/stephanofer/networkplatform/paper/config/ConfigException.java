package com.stephanofer.networkplatform.paper.config;

/** Runtime failure raised by NetworkPlatform configuration operations. */
public class ConfigException extends RuntimeException {

    public ConfigException(final String message) {
        super(message);
    }

    public ConfigException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
