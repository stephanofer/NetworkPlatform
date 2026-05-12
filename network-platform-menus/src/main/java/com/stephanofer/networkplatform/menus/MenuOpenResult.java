package com.stephanofer.networkplatform.menus;

public sealed interface MenuOpenResult permits MenuOpenResult.Opened, MenuOpenResult.Missing, MenuOpenResult.Unavailable, MenuOpenResult.Failed {

    record Opened(MenuKey key) implements MenuOpenResult {}

    record Missing(MenuKey key) implements MenuOpenResult {}

    record Unavailable(String reason) implements MenuOpenResult {}

    record Failed(MenuKey key, Throwable cause) implements MenuOpenResult {}
}
