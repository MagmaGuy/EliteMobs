package com.magmaguy.elitemobs.advancedcombat.menu;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerActionTokenRegistryTest {

    @Test
    void tokensArePlayerBoundAndSingleUse() {
        PlayerActionTokenRegistry registry = new PlayerActionTokenRegistry();
        UUID owner = UUID.randomUUID();
        String token = registry.issue(owner, new ClassMenuAction.OpenOverview());

        assertTrue(registry.consume(UUID.randomUUID(), token).isEmpty());
        assertInstanceOf(ClassMenuAction.OpenOverview.class, registry.consume(owner, token).orElseThrow());
        assertTrue(registry.consume(owner, token).isEmpty());
    }

    @Test
    void openingANewPageInvalidatesAllOldPageActions() {
        PlayerActionTokenRegistry registry = new PlayerActionTokenRegistry();
        UUID playerId = UUID.randomUUID();
        String oldToken = registry.issue(playerId, new ClassMenuAction.OpenOverview());

        registry.beginPage(playerId);

        assertTrue(registry.consume(playerId, oldToken).isEmpty());
        assertEquals(0, registry.size());
    }

    @Test
    void expiredTokensCannotExecute() {
        AtomicLong clock = new AtomicLong(1_000L);
        PlayerActionTokenRegistry registry =
                new PlayerActionTokenRegistry(Duration.ofSeconds(2), clock::get);
        UUID playerId = UUID.randomUUID();
        String token = registry.issue(playerId, new ClassMenuAction.OpenControls());

        clock.set(3_001L);

        assertTrue(registry.consume(playerId, token).isEmpty());
        assertEquals(0, registry.size());
    }
}
