package com.stephanofer.networkplatform.paper.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CommandNode {

    public enum Kind {
        LITERAL,
        ARGUMENT
    }

    private final Kind kind;
    private final String literal;
    private final CommandArgument<?> argument;
    private final List<CommandNode> children = new ArrayList<>();
    private String permission;
    private SenderScope senderScope = SenderScope.ANY;
    private boolean restricted;
    private CommandHandler handler;
    private CommandExceptionHandler exceptionHandler;

    private CommandNode(final Kind kind, final String literal, final CommandArgument<?> argument) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.literal = literal;
        this.argument = argument;
    }

    public static CommandNode literal(final String literal) {
        Objects.requireNonNull(literal, "literal");
        final String normalized = literal.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("literal cannot be blank");
        }
        return new CommandNode(Kind.LITERAL, normalized, null);
    }

    public static CommandNode argument(final CommandArgument<?> argument) {
        return new CommandNode(Kind.ARGUMENT, null, Objects.requireNonNull(argument, "argument"));
    }

    public Kind kind() {
        return this.kind;
    }

    public String literal() {
        return this.literal;
    }

    public CommandArgument<?> argument() {
        return this.argument;
    }

    public List<CommandNode> children() {
        return List.copyOf(this.children);
    }

    public String permission() {
        return this.permission;
    }

    public SenderScope senderScope() {
        return this.senderScope;
    }

    public boolean isRestricted() {
        return this.restricted;
    }

    public CommandHandler handler() {
        return this.handler;
    }

    public CommandExceptionHandler exceptionHandler() {
        return this.exceptionHandler;
    }

    public CommandNode then(final CommandNode child) {
        this.children.add(Objects.requireNonNull(child, "child"));
        return this;
    }

    public CommandNode permission(final String permission) {
        this.permission = normalizeOptional(permission);
        return this;
    }

    public CommandNode senderScope(final SenderScope senderScope) {
        this.senderScope = Objects.requireNonNull(senderScope, "senderScope");
        return this;
    }

    public CommandNode restricted() {
        this.restricted = true;
        return this;
    }

    public CommandNode handler(final CommandHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
        return this;
    }

    public CommandNode onError(final CommandExceptionHandler exceptionHandler) {
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler");
        return this;
    }

    static String normalizeOptional(final String input) {
        if (input == null) {
            return null;
        }
        final String normalized = input.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("value cannot be blank");
        }
        return normalized;
    }
}
