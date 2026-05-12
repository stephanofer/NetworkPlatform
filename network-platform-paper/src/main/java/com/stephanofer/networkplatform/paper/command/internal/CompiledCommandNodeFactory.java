package com.stephanofer.networkplatform.paper.command.internal;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.stephanofer.networkplatform.paper.command.CommandArgument;
import com.stephanofer.networkplatform.paper.command.CommandExceptionHandler;
import com.stephanofer.networkplatform.paper.command.CommandNode;
import com.stephanofer.networkplatform.paper.command.CommandSpec;
import com.stephanofer.networkplatform.paper.command.CommandSuggestion;
import com.stephanofer.networkplatform.paper.command.CommandSuggestionContext;
import com.stephanofer.networkplatform.paper.command.CommandSuggestionProvider;
import com.stephanofer.networkplatform.paper.command.SenderScope;
import com.stephanofer.networkplatform.paper.command.SuggestionSnapshot;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

final class CompiledCommandNodeFactory {

    private final CommandExecutionCoordinator executionCoordinator;

    CompiledCommandNodeFactory(final CommandExecutionCoordinator executionCoordinator) {
        this.executionCoordinator = Objects.requireNonNull(executionCoordinator, "executionCoordinator");
    }

    LiteralCommandNode<CommandSourceStack> compile(final CommandSpec spec) {
        final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(spec.name());
        applyRequirement(root, spec.permission(), spec.isRestricted());
        attachExecution(root, spec.handler(), spec.exceptionHandler(), List.of(), List.of(spec.senderScope()));

        for (final CommandNode child : spec.children()) {
            root.then(compileNode(
                child,
                new ArrayList<>(),
                new ArrayList<>(List.of(spec.senderScope())),
                spec.exceptionHandler()
            ));
        }

        return root.build();
    }

    private ArgumentBuilder<CommandSourceStack, ?> compileNode(
        final CommandNode node,
        final List<CommandArgument<?>> inheritedArguments,
        final List<SenderScope> senderScopes,
        final CommandExceptionHandler inheritedExceptionHandler
    ) {
        final ArgumentBuilder<CommandSourceStack, ?> builder;
        final List<CommandArgument<?>> pathArguments = new ArrayList<>(inheritedArguments);
        final List<SenderScope> pathScopes = new ArrayList<>(senderScopes);
        pathScopes.add(node.senderScope());

        if (node.kind() == CommandNode.Kind.LITERAL) {
            builder = Commands.literal(node.literal());
        } else {
            final CommandArgument<?> argument = node.argument();
            pathArguments.add(argument);
            builder = createArgumentBuilder(argument);
        }

        applyRequirement(builder, node.permission(), node.isRestricted());
        final CommandExceptionHandler exceptionHandler = node.exceptionHandler() == null ? inheritedExceptionHandler : node.exceptionHandler();
        attachExecution(builder, node.handler(), exceptionHandler, pathArguments, pathScopes);

        for (final CommandNode child : node.children()) {
            builder.then(compileNode(child, pathArguments, pathScopes, exceptionHandler));
        }
        return builder;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private RequiredArgumentBuilder<CommandSourceStack, ?> createArgumentBuilder(final CommandArgument<?> argument) {
        final RequiredArgumentBuilder<CommandSourceStack, ?> builder =
            (RequiredArgumentBuilder) Commands.argument(argument.name(), argument.argumentType());
        final CommandSuggestionProvider suggestions = argument.suggestions();
        if (suggestions != null) {
            builder.suggests((context, suggestionsBuilder) -> buildSuggestions(context, suggestionsBuilder, suggestions));
        }
        return builder;
    }

    private CompletableFuture<Suggestions> buildSuggestions(
        final CommandContext<CommandSourceStack> context,
        final SuggestionsBuilder builder,
        final CommandSuggestionProvider provider
    ) {
        return provider.suggestions(new CommandSuggestionContext(context, builder))
            .thenApply(snapshot -> applySuggestions(builder, snapshot))
            .exceptionally(ignored -> builder.build())
            .toCompletableFuture();
    }

    private Suggestions applySuggestions(final SuggestionsBuilder builder, final SuggestionSnapshot snapshot) {
        for (final CommandSuggestion entry : snapshot.filter(builder.getRemainingLowerCase(), 50).entries()) {
            if (entry.tooltip() == null) {
                builder.suggest(entry.value());
            } else {
                builder.suggest(entry.value(), entry.tooltip());
            }
        }
        return builder.build();
    }

    private void applyRequirement(
        final ArgumentBuilder<CommandSourceStack, ?> builder,
        final String permission,
        final boolean restricted
    ) {
        Predicate<CommandSourceStack> predicate = null;
        if (permission != null) {
            predicate = source -> source.getSender().hasPermission(permission);
        }

        if (restricted) {
            predicate = Commands.restricted(predicate == null ? source -> true : predicate);
        }

        if (predicate != null) {
            builder.requires(predicate);
        }
    }

    private void attachExecution(
        final ArgumentBuilder<CommandSourceStack, ?> builder,
        final com.stephanofer.networkplatform.paper.command.CommandHandler handler,
        final CommandExceptionHandler exceptionHandler,
        final List<CommandArgument<?>> arguments,
        final List<SenderScope> senderScopes
    ) {
        if (handler == null) {
            return;
        }

        builder.executes(context -> this.executionCoordinator.execute(
            context,
            List.copyOf(arguments),
            List.copyOf(senderScopes),
            handler,
            exceptionHandler
        ));
    }
}
