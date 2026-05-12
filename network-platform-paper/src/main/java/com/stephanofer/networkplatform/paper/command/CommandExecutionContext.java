package com.stephanofer.networkplatform.paper.command;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface CommandExecutionContext {

    CommandContext<CommandSourceStack> brigadier();

    CommandSourceStack source();

    default CommandSender sender() {
        return source().getSender();
    }

    default Entity executor() {
        return source().getExecutor();
    }

    default Location location() {
        return source().getLocation();
    }

    <T> T argument(String name, Class<T> type);

    boolean hasArgument(String name);

    default Player player() {
        return requirePlayer();
    }

    default Player requirePlayer() {
        if (executor() instanceof Player player) {
            return player;
        }

        throw CommandException.rich("<red>Only players can use this command.");
    }

    default void requireSenderScope(final SenderScope scope) {
        Objects.requireNonNull(scope, "scope");
        if (!scope.matches(source())) {
            throw CommandException.rich(scope.failureMessage());
        }
    }

    default void reply(final Component message) {
        sender().sendMessage(Objects.requireNonNull(message, "message"));
    }

    default void replyRich(final String miniMessage) {
        sender().sendRichMessage(Objects.requireNonNull(miniMessage, "miniMessage"));
    }

    default void fail(final Component message) {
        throw CommandException.component(message);
    }

    default void failRich(final String miniMessage) {
        throw CommandException.rich(miniMessage);
    }

    default void noPermission() {
        throw CommandException.rich("<red>You do not have permission to use this command.");
    }

    default void invalidUsage() {
        throw CommandException.rich("<red>Invalid command usage.");
    }
}
