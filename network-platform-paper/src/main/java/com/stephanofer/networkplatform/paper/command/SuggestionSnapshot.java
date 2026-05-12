package com.stephanofer.networkplatform.paper.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public record SuggestionSnapshot(List<CommandSuggestion> entries) {

    public SuggestionSnapshot {
        Objects.requireNonNull(entries, "entries");
        entries = List.copyOf(entries);
    }

    public static SuggestionSnapshot empty() {
        return new SuggestionSnapshot(List.of());
    }

    public static SuggestionSnapshot ofStrings(final Collection<String> values) {
        Objects.requireNonNull(values, "values");
        final List<CommandSuggestion> suggestions = new ArrayList<>(values.size());
        for (final String value : values) {
            if (value != null && !value.isBlank()) {
                suggestions.add(CommandSuggestion.of(value));
            }
        }
        return new SuggestionSnapshot(suggestions);
    }

    public SuggestionSnapshot filter(final String remainingLowerCase, final int limit) {
        final String prefix = remainingLowerCase == null ? "" : remainingLowerCase.toLowerCase(Locale.ROOT);
        final List<CommandSuggestion> filtered = new ArrayList<>();
        for (final CommandSuggestion suggestion : this.entries) {
            if (suggestion.value().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                filtered.add(suggestion);
                if (filtered.size() >= limit) {
                    break;
                }
            }
        }
        return new SuggestionSnapshot(filtered);
    }
}
