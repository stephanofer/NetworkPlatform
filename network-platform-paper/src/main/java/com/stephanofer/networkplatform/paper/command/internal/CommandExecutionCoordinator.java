package com.stephanofer.networkplatform.paper.command.internal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.stephanofer.networkplatform.paper.command.CommandArgument;
import com.stephanofer.networkplatform.paper.command.CommandException;
import com.stephanofer.networkplatform.paper.command.CommandExceptionHandler;
import com.stephanofer.networkplatform.paper.command.CommandExecutionContext;
import com.stephanofer.networkplatform.paper.command.CommandHandler;
import com.stephanofer.networkplatform.paper.command.SenderScope;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CommandExecutionCoordinator {

    private final Logger logger;

    CommandExecutionCoordinator(final Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    int execute(
        final CommandContext<CommandSourceStack> brigadier,
        final List<CommandArgument<?>> arguments,
        final List<SenderScope> senderScopes,
        final CommandHandler handler,
        final CommandExceptionHandler exceptionHandler
    ) throws CommandSyntaxException {
        final Map<String, Object> resolvedArguments = new LinkedHashMap<>();
        for (final CommandArgument<?> argument : arguments) {
            resolvedArguments.put(argument.name(), argument.resolver().resolve(brigadier, argument.name()));
        }

        final CommandExecutionContext context = new DefaultCommandExecutionContext(brigadier, resolvedArguments);

        try {
            for (final SenderScope senderScope : senderScopes) {
                context.requireSenderScope(senderScope);
            }

            return handler.handle(context);
        } catch (final CommandException commandException) {
            send(context, commandException);
            return Command.SINGLE_SUCCESS;
        } catch (final CommandSyntaxException syntaxException) {
            throw syntaxException;
        } catch (final Throwable throwable) {
            if (exceptionHandler != null && exceptionHandler.handle(context, throwable)) {
                return Command.SINGLE_SUCCESS;
            }

            this.logger.log(Level.SEVERE, "NetworkPlatform command execution failed", throwable);
            context.replyRich("<red>An internal command error occurred.");
            return Command.SINGLE_SUCCESS;
        }
    }

    private static void send(final CommandExecutionContext context, final CommandException commandException) {
        if (commandException.componentMessage() != null) {
            context.reply(commandException.componentMessage());
            return;
        }
        context.replyRich(commandException.richMessage());
    }
}
