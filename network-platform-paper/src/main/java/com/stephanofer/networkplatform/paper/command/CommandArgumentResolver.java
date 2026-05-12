package com.stephanofer.networkplatform.paper.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;

@FunctionalInterface
public interface CommandArgumentResolver<T> {

    T resolve(CommandContext<CommandSourceStack> context, String argumentName) throws CommandSyntaxException;
}
