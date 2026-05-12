package com.stephanofer.networkplatform.paper.command;

@FunctionalInterface
public interface CommandExceptionHandler {

    /**
     * @return true if the exception was handled and default processing should stop.
     */
    boolean handle(CommandExecutionContext context, Throwable throwable);
}
