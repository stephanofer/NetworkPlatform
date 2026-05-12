package com.stephanofer.networkplatform.menus;

public sealed interface DialogOpenResult permits DialogOpenResult.Opened, DialogOpenResult.Missing, DialogOpenResult.Unavailable, DialogOpenResult.Failed {

    record Opened(DialogKey key) implements DialogOpenResult {}

    record Missing(DialogKey key) implements DialogOpenResult {}

    record Unavailable(String reason) implements DialogOpenResult {}

    record Failed(DialogKey key, Throwable cause) implements DialogOpenResult {}
}
