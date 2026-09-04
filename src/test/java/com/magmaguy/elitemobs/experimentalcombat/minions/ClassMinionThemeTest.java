package com.magmaguy.elitemobs.experimentalcombat.minions;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassMinionThemeTest {

    @Test
    void resolvesEverySummoningBranchWithoutAbilitySpecificRuntimeScripts() {
        assertEquals(ClassMinionTheme.UNDEAD,
                ClassMinionTheme.forAbility("necromancer.signature").orElseThrow());
        assertEquals(ClassMinionTheme.UNDEAD,
                ClassMinionTheme.forAbility("lich.signature").orElseThrow());
        assertEquals(ClassMinionTheme.UNDEAD,
                ClassMinionTheme.forAbility("plaguebringer.signature").orElseThrow());
        assertEquals(ClassMinionTheme.ANIMAL,
                ClassMinionTheme.forAbility("summoner.signature").orElseThrow());
        assertEquals(ClassMinionTheme.NETHER,
                ClassMinionTheme.forAbility("demonologist.signature").orElseThrow());
        assertEquals(ClassMinionTheme.SPIRIT,
                ClassMinionTheme.forAbility("spiritbinder.signature").orElseThrow());
        assertTrue(ClassMinionTheme.forAbility("mage.signature").isEmpty());
    }

    @Test
    void carrierPoolsMatchClassFantasyAndLocomotion() {
        assertTrue(ClassMinionTheme.UNDEAD.carriers().stream()
                .allMatch(carrier -> switch (carrier.entityType()) {
                    case ZOMBIE, SKELETON, HUSK -> true;
                    default -> false;
                }));
        assertTrue(ClassMinionTheme.ANIMAL.carriers().stream()
                .allMatch(carrier -> switch (carrier.entityType()) {
                    case WOLF, FOX, POLAR_BEAR -> true;
                    default -> false;
                }));
        assertTrue(ClassMinionTheme.NETHER.carriers().stream()
                .allMatch(carrier -> switch (carrier.entityType()) {
                    case PIGLIN_BRUTE, HOGLIN, BLAZE -> true;
                    default -> false;
                }));
        assertEquals(EntityType.VEX, ClassMinionTheme.SPIRIT.carrierFor(99).entityType());
        assertTrue(ClassMinionTheme.SPIRIT.supportRole());
    }
}
