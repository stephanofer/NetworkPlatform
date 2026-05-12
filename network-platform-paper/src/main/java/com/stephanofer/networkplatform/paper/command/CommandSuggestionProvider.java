package com.stephanofer.networkplatform.paper.command;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface CommandSuggestionProvider {

    CompletionStage<SuggestionSnapshot> suggestions(CommandSuggestionContext context);
}
