package com.stephanofer.networkplatform.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record OpenOptions(
    int page,
    boolean preserveHistory,
    List<String> arguments,
    boolean requireMainThread
) {

    public OpenOptions {
        if (page < 1) {
            throw new IllegalArgumentException("page must be >= 1");
        }

        Objects.requireNonNull(arguments, "arguments");
        arguments = List.copyOf(new ArrayList<>(arguments));
    }

    public static OpenOptions defaults() {
        return new OpenOptions(1, true, List.of(), true);
    }

    public static OpenOptions page(final int page) {
        return new OpenOptions(page, true, List.of(), true);
    }

    public OpenOptions withHistoryPreserved() {
        return new OpenOptions(this.page, true, this.arguments, this.requireMainThread);
    }

    public OpenOptions withoutHistory() {
        return new OpenOptions(this.page, false, this.arguments, this.requireMainThread);
    }

    public OpenOptions args(final String... arguments) {
        Objects.requireNonNull(arguments, "arguments");
        return new OpenOptions(this.page, this.preserveHistory, List.of(arguments), this.requireMainThread);
    }

    public OpenOptions args(final List<String> arguments) {
        return new OpenOptions(this.page, this.preserveHistory, arguments, this.requireMainThread);
    }

    public OpenOptions requireMainThread(final boolean requireMainThread) {
        return new OpenOptions(this.page, this.preserveHistory, this.arguments, requireMainThread);
    }
}
