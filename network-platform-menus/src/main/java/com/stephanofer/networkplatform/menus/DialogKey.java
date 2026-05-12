package com.stephanofer.networkplatform.menus;

import java.util.Objects;

public record DialogKey(String value) {

    public DialogKey {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("dialog key cannot be blank");
        }
    }

    public static DialogKey of(final String value) {
        return new DialogKey(value);
    }
}
