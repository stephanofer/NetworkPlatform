package com.stephanofer.networkplatform.paper.command;

import java.util.Objects;
import net.kyori.adventure.text.Component;

public final class CommandException extends RuntimeException {

    private final Component componentMessage;
    private final String richMessage;

    private CommandException(final Component componentMessage, final String richMessage) {
        super(null, null, false, false);
        this.componentMessage = componentMessage;
        this.richMessage = richMessage;
    }

    public static CommandException component(final Component componentMessage) {
        return new CommandException(Objects.requireNonNull(componentMessage, "componentMessage"), null);
    }

    public static CommandException rich(final String richMessage) {
        return new CommandException(null, Objects.requireNonNull(richMessage, "richMessage"));
    }

    public Component componentMessage() {
        return this.componentMessage;
    }

    public String richMessage() {
        return this.richMessage;
    }
}
