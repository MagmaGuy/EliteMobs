package com.magmaguy.elitemobs.advancedcombat.minions;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorpseOpportunityLedgerTest {

    @Test
    void eachEligibleNecromancerCanConsumeOneSharedCorpseWithoutPartyCompetition() {
        CorpseOpportunityLedger ledger = new CorpseOpportunityLedger();
        UUID corpse = UUID.randomUUID();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        ledger.offer(corpse, Set.of(first, second), 700L);

        assertTrue(ledger.consume(corpse, first, 200L));
        assertFalse(ledger.consume(corpse, first, 201L));
        assertTrue(ledger.availableTo(corpse, second, 699L));
        assertTrue(ledger.consume(corpse, second, 699L));
        assertFalse(ledger.contains(corpse));
    }

    @Test
    void opportunityExpiresAtItsExactDeadlineAndCannotBeRevivedByConsumption() {
        CorpseOpportunityLedger ledger = new CorpseOpportunityLedger();
        UUID corpse = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        ledger.offer(corpse, Set.of(player), 600L);

        assertTrue(ledger.availableTo(corpse, player, 599L));
        assertFalse(ledger.availableTo(corpse, player, 600L));
        assertFalse(ledger.consume(corpse, player, 600L));
        assertFalse(ledger.contains(corpse));
    }

    @Test
    void ineligiblePlayersCanNeverConsumeTheMarker() {
        CorpseOpportunityLedger ledger = new CorpseOpportunityLedger();
        UUID corpse = UUID.randomUUID();
        ledger.offer(corpse, Set.of(UUID.randomUUID()), 600L);

        assertFalse(ledger.consume(corpse, UUID.randomUUID(), 100L));
        assertTrue(ledger.contains(corpse));
    }
}
