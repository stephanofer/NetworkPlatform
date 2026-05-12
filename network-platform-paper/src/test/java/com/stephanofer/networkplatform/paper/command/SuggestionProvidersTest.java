package com.stephanofer.networkplatform.paper.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class SuggestionProvidersTest {

    @Test
    void shouldFilterSnapshotsByPrefixAndLimit() {
        final SuggestionSnapshot snapshot = SuggestionSnapshot.ofStrings(List.of("solo", "duo", "squad", "sumo"))
            .filter("s", 2);

        assertEquals(List.of("solo", "squad"), snapshot.entries().stream().map(CommandSuggestion::value).toList());
    }
}
