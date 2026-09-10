package com.magmaguy.elitemobs.advancedcombat.abilities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetaliationBudgetTest {
    @Test
    void accumulatesUpToTheHealthBoundAndKeepsCurrentOwnership() {
        UUID firstAttacker = UUID.randomUUID();
        UUID lastAttacker = UUID.randomUUID();
        RetaliationBudget first = RetaliationBudget.accumulate(
                null, 30D, 50D, "justicar.signature", firstAttacker);
        RetaliationBudget capped = RetaliationBudget.accumulate(
                first, 40D, 50D, "justicar.signature", lastAttacker);

        assertEquals(50D, capped.amount());
        assertEquals("justicar.signature", capped.abilityId());
        assertEquals(lastAttacker, capped.lastAttackerId());
    }

    @Test
    void changingTheOwningAbilityStartsAFreshBudget() {
        RetaliationBudget justicar = RetaliationBudget.accumulate(
                null, 20D, 100D, "justicar.signature", UUID.randomUUID());
        RetaliationBudget templar = RetaliationBudget.accumulate(
                justicar, 10D, 100D, "templar.signature", UUID.randomUUID());

        assertEquals(10D, templar.amount());
        assertEquals("templar.signature", templar.abilityId());
    }
}
