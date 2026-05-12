package com.stephanofer.networkplatform.database;

import java.time.Duration;
import java.util.Objects;

public record DatabasePoolConfig(
    int maximumPoolSize,
    int minimumIdle,
    Duration connectionTimeout,
    Duration validationTimeout,
    Duration idleTimeout,
    Duration maxLifetime,
    Duration keepaliveTime,
    Duration leakDetectionThreshold
) {

    private static final int MAXIMUM_ALLOWED_POOL_SIZE = 64;
    private static final Duration MINIMUM_CONNECTION_TIMEOUT = Duration.ofMillis(250);
    private static final Duration MINIMUM_VALIDATION_TIMEOUT = Duration.ofMillis(250);
    private static final Duration MINIMUM_IDLE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration MINIMUM_MAX_LIFETIME = Duration.ofSeconds(30);
    private static final Duration MINIMUM_KEEPALIVE_TIME = Duration.ofSeconds(30);

    public DatabasePoolConfig {
        connectionTimeout = requireDuration(connectionTimeout, "connectionTimeout");
        validationTimeout = requireDuration(validationTimeout, "validationTimeout");
        idleTimeout = requireDuration(idleTimeout, "idleTimeout");
        maxLifetime = requireDuration(maxLifetime, "maxLifetime");
        keepaliveTime = requireDuration(keepaliveTime, "keepaliveTime");
        leakDetectionThreshold = requireNonNegative(leakDetectionThreshold, "leakDetectionThreshold");

        if (maximumPoolSize < 1) {
            throw new DatabaseModuleConfigException("maximumPoolSize must be at least 1");
        }
        if (maximumPoolSize > MAXIMUM_ALLOWED_POOL_SIZE) {
            throw new DatabaseModuleConfigException(
                "maximumPoolSize must be less than or equal to " + MAXIMUM_ALLOWED_POOL_SIZE
            );
        }
        if (minimumIdle < 0) {
            throw new DatabaseModuleConfigException("minimumIdle cannot be negative");
        }
        if (minimumIdle > maximumPoolSize) {
            throw new DatabaseModuleConfigException("minimumIdle cannot be greater than maximumPoolSize");
        }
        if (connectionTimeout.compareTo(MINIMUM_CONNECTION_TIMEOUT) < 0) {
            throw new DatabaseModuleConfigException("connectionTimeout must be at least 250ms");
        }
        if (validationTimeout.compareTo(MINIMUM_VALIDATION_TIMEOUT) < 0) {
            throw new DatabaseModuleConfigException("validationTimeout must be at least 250ms");
        }
        if (!idleTimeout.isZero() && idleTimeout.compareTo(MINIMUM_IDLE_TIMEOUT) < 0) {
            throw new DatabaseModuleConfigException("idleTimeout must be zero or at least 10s");
        }
        if (maxLifetime.compareTo(MINIMUM_MAX_LIFETIME) < 0) {
            throw new DatabaseModuleConfigException("maxLifetime must be at least 30s");
        }
        if (keepaliveTime.compareTo(MINIMUM_KEEPALIVE_TIME) < 0) {
            throw new DatabaseModuleConfigException("keepaliveTime must be at least 30s");
        }
        if (keepaliveTime.compareTo(maxLifetime) >= 0) {
            throw new DatabaseModuleConfigException("keepaliveTime must be lower than maxLifetime");
        }
        if (!leakDetectionThreshold.isZero() && leakDetectionThreshold.compareTo(MINIMUM_CONNECTION_TIMEOUT) < 0) {
            throw new DatabaseModuleConfigException("leakDetectionThreshold must be zero or at least 250ms");
        }
    }

    public static DatabasePoolConfig defaults() {
        return new Builder().build();
    }

    public Builder toBuilder() {
        return new Builder()
            .maximumPoolSize(this.maximumPoolSize)
            .minimumIdle(this.minimumIdle)
            .connectionTimeout(this.connectionTimeout)
            .validationTimeout(this.validationTimeout)
            .idleTimeout(this.idleTimeout)
            .maxLifetime(this.maxLifetime)
            .keepaliveTime(this.keepaliveTime)
            .leakDetectionThreshold(this.leakDetectionThreshold);
    }

    public static final class Builder {

        private int maximumPoolSize = 10;
        private int minimumIdle = 2;
        private Duration connectionTimeout = Duration.ofSeconds(10);
        private Duration validationTimeout = Duration.ofSeconds(5);
        private Duration idleTimeout = Duration.ofMinutes(10);
        private Duration maxLifetime = Duration.ofMinutes(25);
        private Duration keepaliveTime = Duration.ofMinutes(5);
        private Duration leakDetectionThreshold = Duration.ZERO;

        public Builder maximumPoolSize(final int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
            return this;
        }

        public Builder minimumIdle(final int minimumIdle) {
            this.minimumIdle = minimumIdle;
            return this;
        }

        public Builder connectionTimeout(final Duration connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder validationTimeout(final Duration validationTimeout) {
            this.validationTimeout = validationTimeout;
            return this;
        }

        public Builder idleTimeout(final Duration idleTimeout) {
            this.idleTimeout = idleTimeout;
            return this;
        }

        public Builder maxLifetime(final Duration maxLifetime) {
            this.maxLifetime = maxLifetime;
            return this;
        }

        public Builder keepaliveTime(final Duration keepaliveTime) {
            this.keepaliveTime = keepaliveTime;
            return this;
        }

        public Builder leakDetectionThreshold(final Duration leakDetectionThreshold) {
            this.leakDetectionThreshold = leakDetectionThreshold;
            return this;
        }

        public DatabasePoolConfig build() {
            return new DatabasePoolConfig(
                this.maximumPoolSize,
                this.minimumIdle,
                this.connectionTimeout,
                this.validationTimeout,
                this.idleTimeout,
                this.maxLifetime,
                this.keepaliveTime,
                this.leakDetectionThreshold
            );
        }
    }

    private static Duration requireDuration(final Duration value, final String name) {
        Objects.requireNonNull(value, name);
        if (value.isNegative()) {
            throw new DatabaseModuleConfigException(name + " cannot be negative");
        }
        return value;
    }

    private static Duration requireNonNegative(final Duration value, final String name) {
        Objects.requireNonNull(value, name);
        if (value.isNegative()) {
            throw new DatabaseModuleConfigException(name + " cannot be negative");
        }
        return value;
    }
}
