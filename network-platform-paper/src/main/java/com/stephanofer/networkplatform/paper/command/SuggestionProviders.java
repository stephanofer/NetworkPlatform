package com.stephanofer.networkplatform.paper.command;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SuggestionProviders {

    public static final int DEFAULT_LIMIT = 50;

    private SuggestionProviders() {
    }

    public static CommandSuggestionProvider staticValues(final String... values) {
        Objects.requireNonNull(values, "values");
        return snapshot(() -> List.of(values));
    }

    public static CommandSuggestionProvider staticValues(final Collection<String> values) {
        Objects.requireNonNull(values, "values");
        final List<String> copy = List.copyOf(values);
        return snapshot(() -> copy);
    }

    public static CommandSuggestionProvider snapshot(final Supplier<? extends Collection<String>> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        return context -> CompletableFuture.completedFuture(
            SuggestionSnapshot.ofStrings(supplier.get()).filter(context.remainingLowerCase(), DEFAULT_LIMIT)
        );
    }

    public static CommandSuggestionProvider cached(final Duration ttl, final CommandSuggestionProvider delegate) {
        Objects.requireNonNull(ttl, "ttl");
        Objects.requireNonNull(delegate, "delegate");
        if (ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("ttl must be positive");
        }

        final AtomicReference<CacheEntry> cache = new AtomicReference<>();
        return context -> {
            final CacheEntry current = cache.get();
            final Instant now = Instant.now();
            if (current != null && now.isBefore(current.expiresAt())) {
                return CompletableFuture.completedFuture(current.snapshot().filter(context.remainingLowerCase(), DEFAULT_LIMIT));
            }

            return delegate.suggestions(context).thenApply(snapshot -> {
                cache.set(new CacheEntry(snapshot, now.plus(ttl)));
                return snapshot.filter(context.remainingLowerCase(), DEFAULT_LIMIT);
            });
        };
    }

    public static CommandSuggestionProvider onlinePlayers() {
        return context -> {
            final List<String> names = new ArrayList<>();
            for (final Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return CompletableFuture.completedFuture(
                SuggestionSnapshot.ofStrings(names).filter(context.remainingLowerCase(), DEFAULT_LIMIT)
            );
        };
    }

    public static CommandSuggestionProvider custom(final CommandSuggestionProvider provider) {
        return Objects.requireNonNull(provider, "provider");
    }

    private record CacheEntry(SuggestionSnapshot snapshot, Instant expiresAt) {
    }
}
