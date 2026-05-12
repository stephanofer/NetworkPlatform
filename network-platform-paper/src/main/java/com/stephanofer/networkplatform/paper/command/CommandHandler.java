package com.stephanofer.networkplatform.paper.command;

@FunctionalInterface
public interface CommandHandler {

    int handle(CommandExecutionContext context) throws Exception;
}
