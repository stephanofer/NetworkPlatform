package com.stephanofer.networkplatform.paper.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class CommandSpec {

    private final String name;
    private final List<String> aliases;
    private final String description;
    private final String usage;
    private final String permission;
    private final SenderScope senderScope;
    private final boolean restricted;
    private final List<CommandNode> children;
    private final CommandHandler handler;
    private final CommandExceptionHandler exceptionHandler;

    private CommandSpec(final Builder builder) {
        this.name = builder.name;
        this.aliases = List.copyOf(builder.aliases);
        this.description = builder.description;
        this.usage = builder.usage;
        this.permission = builder.permission;
        this.senderScope = builder.senderScope;
        this.restricted = builder.restricted;
        this.children = List.copyOf(builder.children);
        this.handler = builder.handler;
        this.exceptionHandler = builder.exceptionHandler;
    }

    public static Builder builder(final String name) {
        return new Builder(name);
    }

    public String name() {
        return this.name;
    }

    public List<String> aliases() {
        return this.aliases;
    }

    public String description() {
        return this.description;
    }

    public String usage() {
        return this.usage;
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

    public List<CommandNode> children() {
        return this.children;
    }

    public CommandHandler handler() {
        return this.handler;
    }

    public CommandExceptionHandler exceptionHandler() {
        return this.exceptionHandler;
    }

    public static final class Builder {

        private final String name;
        private final Set<String> aliases = new LinkedHashSet<>();
        private final List<CommandNode> children = new ArrayList<>();
        private String description;
        private String usage;
        private String permission;
        private SenderScope senderScope = SenderScope.ANY;
        private boolean restricted;
        private CommandHandler handler;
        private CommandExceptionHandler exceptionHandler;

        private Builder(final String name) {
            Objects.requireNonNull(name, "name");
            final String normalized = name.trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("command name cannot be blank");
            }
            this.name = normalized;
        }

        public Builder aliases(final String... aliases) {
            Objects.requireNonNull(aliases, "aliases");
            for (final String alias : aliases) {
                this.aliases.add(requireAlias(alias));
            }
            return this;
        }

        public Builder aliases(final Collection<String> aliases) {
            Objects.requireNonNull(aliases, "aliases");
            for (final String alias : aliases) {
                this.aliases.add(requireAlias(alias));
            }
            return this;
        }

        public Builder description(final String description) {
            this.description = CommandNode.normalizeOptional(description);
            return this;
        }

        public Builder usage(final String usage) {
            this.usage = CommandNode.normalizeOptional(usage);
            return this;
        }

        public Builder permission(final String permission) {
            this.permission = CommandNode.normalizeOptional(permission);
            return this;
        }

        public Builder senderScope(final SenderScope senderScope) {
            this.senderScope = Objects.requireNonNull(senderScope, "senderScope");
            return this;
        }

        public Builder restricted() {
            this.restricted = true;
            return this;
        }

        public Builder then(final CommandNode child) {
            this.children.add(Objects.requireNonNull(child, "child"));
            return this;
        }

        public Builder handler(final CommandHandler handler) {
            this.handler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public Builder onError(final CommandExceptionHandler exceptionHandler) {
            this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler");
            return this;
        }

        public CommandSpec build() {
            this.aliases.removeIf(alias -> alias.equalsIgnoreCase(this.name));
            return new CommandSpec(this);
        }

        private static String requireAlias(final String alias) {
            Objects.requireNonNull(alias, "alias");
            final String normalized = alias.trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("alias cannot be blank");
            }
            return normalized;
        }
    }
}
