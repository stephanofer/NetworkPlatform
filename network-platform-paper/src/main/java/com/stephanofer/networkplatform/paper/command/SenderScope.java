package com.stephanofer.networkplatform.paper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public enum SenderScope {
    ANY {
        @Override
        public boolean matches(final CommandSourceStack source) {
            return true;
        }

        @Override
        public String failureMessage() {
            return "<red>This command cannot be used from here.";
        }
    },
    PLAYER {
        @Override
        public boolean matches(final CommandSourceStack source) {
            return source.getExecutor() instanceof Player;
        }

        @Override
        public String failureMessage() {
            return "<red>Only players can use this command.";
        }
    },
    CONSOLE {
        @Override
        public boolean matches(final CommandSourceStack source) {
            return source.getSender() instanceof ConsoleCommandSender;
        }

        @Override
        public String failureMessage() {
            return "<red>Only console can use this command.";
        }
    },
    ENTITY {
        @Override
        public boolean matches(final CommandSourceStack source) {
            return source.getExecutor() != null;
        }

        @Override
        public String failureMessage() {
            return "<red>Only entities can use this command.";
        }
    };

    public abstract boolean matches(CommandSourceStack source);

    public abstract String failureMessage();
}
