package com.stephanofer.networkplatform.paper.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.stephanofer.networkplatform.paper.command.internal.DurationParser;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class CommandArguments {

    private static final SimpleCommandExceptionType ERROR_PLAYER_NOT_FOUND = new SimpleCommandExceptionType(
        new com.mojang.brigadier.LiteralMessage("Player not found")
    );
    private static final SimpleCommandExceptionType ERROR_MULTIPLE_PLAYERS = new SimpleCommandExceptionType(
        new com.mojang.brigadier.LiteralMessage("Expected exactly one player")
    );

    private CommandArguments() {
    }

    public static CommandArgument<String> word(final String name) {
        return raw(name, String.class, StringArgumentType.word());
    }

    public static CommandArgument<String> string(final String name) {
        return raw(name, String.class, StringArgumentType.string());
    }

    public static CommandArgument<String> greedyString(final String name) {
        return raw(name, String.class, StringArgumentType.greedyString());
    }

    public static CommandArgument<Integer> integer(final String name) {
        return raw(name, Integer.class, IntegerArgumentType.integer());
    }

    public static CommandArgument<Integer> integer(final String name, final int min, final int max) {
        return raw(name, Integer.class, IntegerArgumentType.integer(min, max));
    }

    public static CommandArgument<Long> longArg(final String name) {
        return raw(name, Long.class, LongArgumentType.longArg());
    }

    public static CommandArgument<Double> doubleArg(final String name) {
        return raw(name, Double.class, DoubleArgumentType.doubleArg());
    }

    public static CommandArgument<Boolean> bool(final String name) {
        return raw(name, Boolean.class, BoolArgumentType.bool());
    }

    public static <E extends Enum<E>> CommandArgument<E> enumArg(final String name, final Class<E> enumType) {
        Objects.requireNonNull(enumType, "enumType");
        final CommandArgument<E> argument = custom(
            name,
            enumType,
            StringArgumentType.word(),
            (context, argumentName) -> parseEnum(enumType, context.getArgument(argumentName, String.class))
        );

        final List<String> suggestions = new ArrayList<>();
        for (final E constant : enumType.getEnumConstants()) {
            suggestions.add(constant.name().toLowerCase(Locale.ROOT));
        }
        return argument.suggestions(SuggestionProviders.staticValues(suggestions));
    }

    public static CommandArgument<String> onlinePlayerName(final String name) {
        return custom(name, String.class, StringArgumentType.word(), (context, argumentName) -> {
            final String input = context.getArgument(argumentName, String.class);
            for (final Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().equalsIgnoreCase(input)) {
                    return player.getName();
                }
            }
            throw ERROR_PLAYER_NOT_FOUND.create();
        }).suggestions(SuggestionProviders.onlinePlayers());
    }

    public static CommandArgument<Player> onlinePlayer(final String name) {
        return custom(name, Player.class, ArgumentTypes.player(), CommandArguments::resolveSinglePlayer)
            .suggestions(SuggestionProviders.onlinePlayers());
    }

    public static CommandArgument<Duration> duration(final String name) {
        return custom(name, Duration.class, StringArgumentType.word(), (context, argumentName) ->
            DurationParser.parse(context.getArgument(argumentName, String.class))
        );
    }

    public static <T> CommandArgument<T> raw(final String name, final Class<T> type, final ArgumentType<T> argumentType) {
        return custom(name, type, argumentType, (context, argumentName) -> context.getArgument(argumentName, type));
    }

    public static <T> CommandArgument<T> custom(
        final String name,
        final Class<T> type,
        final ArgumentType<?> argumentType,
        final CommandArgumentResolver<T> resolver
    ) {
        return new CommandArgument<>(name, type, argumentType, resolver);
    }

    private static Player resolveSinglePlayer(
        final CommandContext<CommandSourceStack> context,
        final String argumentName
    ) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver resolver = context.getArgument(argumentName, PlayerSelectorArgumentResolver.class);
        final List<Player> players = resolver.resolve(context.getSource());
        if (players.isEmpty()) {
            throw ERROR_PLAYER_NOT_FOUND.create();
        }
        if (players.size() > 1) {
            throw ERROR_MULTIPLE_PLAYERS.create();
        }
        return players.getFirst();
    }

    private static <E extends Enum<E>> E parseEnum(final Class<E> enumType, final String input) throws CommandSyntaxException {
        for (final E constant : enumType.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(input)) {
                return constant;
            }
        }

        throw new SimpleCommandExceptionType(
            new com.mojang.brigadier.LiteralMessage("Unknown value: " + input)
        ).create();
    }
}
