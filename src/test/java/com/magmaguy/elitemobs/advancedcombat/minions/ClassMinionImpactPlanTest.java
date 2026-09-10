package com.magmaguy.elitemobs.advancedcombat.minions;

import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilityRegistry;
import com.magmaguy.elitemobs.advancedcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassMinionImpactPlanTest {
    private final FixedAbilityRegistry registry = BuiltInClassContent.abilityRegistry();

    @Test
    void summonImpactFantasyComesFromCanonicalEffectsRatherThanAbilityIds() {
        assertEquals(new ClassMinionImpactPlan(.14D, Set.of()),
                plan("necromancer.signature"));
        assertEquals(new ClassMinionImpactPlan(.14D, Set.of(AbilityEffect.WEAKEN)),
                plan("lich.signature"));
        assertEquals(new ClassMinionImpactPlan(0D,
                        Set.of(AbilityEffect.WEAKEN, AbilityEffect.SLOW)),
                plan("plaguebringer.signature"));
        assertEquals(new ClassMinionImpactPlan(0D, Set.of(AbilityEffect.INTERRUPT)),
                plan("summoner.signature"));
        assertEquals(new ClassMinionImpactPlan(0D, Set.of(AbilityEffect.FEAR)),
                plan("demonologist.signature"));
        assertEquals(new ClassMinionImpactPlan(0D, Set.of()),
                plan("spiritbinder.signature"));
    }

    private ClassMinionImpactPlan plan(String abilityId) {
        return ClassMinionImpactPlan.from(registry.require(abilityId));
    }
}
