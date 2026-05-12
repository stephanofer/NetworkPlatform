package com.stephanofer.networkplatform.menus;

import java.util.Objects;

public record MenuKey(String value) {

    public MenuKey {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("menu key cannot be blank");
        }
    }

    public static MenuKey of(final String value) {
        return new MenuKey(value);
    }
}
