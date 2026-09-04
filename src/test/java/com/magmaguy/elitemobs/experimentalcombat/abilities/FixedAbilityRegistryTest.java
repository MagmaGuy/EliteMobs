package com.magmaguy.elitemobs.experimentalcombat.abilities;

import com.magmaguy.elitemobs.experimentalcombat.classes.AbilitySlot;
import com.magmaguy.elitemobs.experimentalcombat.classes.BuiltInClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.classes.ClassCatalog;
import com.magmaguy.elitemobs.experimentalcombat.content.BuiltInClassContent;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedAbilityRegistryTest {

    private final ClassCatalog catalog = BuiltInClassCatalog.catalog();
    private final FixedAbilityRegistry registry = BuiltInClassContent.abilityRegistry();

    @Test
    void registryCoversEveryCatalogAbilityExactlyOnce() {
        assertEquals(catalog.roots().size() + catalog.forms().size() * 2,
                registry.registeredIds().size());
        assertEquals(catalog.roots().size(), count(AbilitySlot.MOBILITY));
        assertEquals(catalog.forms().size(), count(AbilitySlot.SIGNATURE));
        assertEquals(catalog.forms().size(), count(AbilitySlot.UTILITY));
    }

    @Test
    void allReusableExecutionFamiliesHaveFixedEntries() {
        assertEquals(Set.of(AbilityFamily.values()), registry.registeredIds().stream()
                .map(registry::require)
                .map(FixedAbilitySpec::family)
                .collect(Collectors.toSet()));
    }

    @Test
    void signatureMechanicsRequiredByTheBaselineAreExplicit() {
        FixedAbilitySpec steed = registry.require("paladin.mobility");
        assertEquals("MOUNTED_CHARGE", steed.family().name());
        assertEquals(100, steed.tuning().durationTicks());
        assertEquals(40D, steed.tuning().range());
        assertEquals(1.25D, steed.tuning().radius());
        assertEquals(55D, steed.resourceCost());
        assertTrue(steed.effects().containsAll(Set.of(
                AbilityEffect.DAMAGE, AbilityEffect.KNOCKBACK, AbilityEffect.TAUNT)));

        FixedAbilitySpec leap = registry.require("berserker.mobility");
        assertEquals(AbilityFamily.BALLISTIC_LEAP, leap.family());
        assertTrue(leap.effects().containsAll(Set.of(
                AbilityEffect.DAMAGE, AbilityEffect.LAUNCH, AbilityEffect.INTERRUPT)));

        FixedAbilitySpec mark = registry.require("ranger.utility");
        assertEquals(1.10D, mark.tuning().modifierMultiplier());
        assertTrue(mark.effects().contains(AbilityEffect.PARTY_DAMAGE_MARK));

        FixedAbilitySpec flight = registry.require("cleric.mobility");
        assertEquals(AbilityFamily.ALLY_FLIGHT, flight.family());
        assertEquals(18D, flight.tuning().range());

        FixedAbilitySpec blink = registry.require("spellcaster.mobility");
        assertEquals(AbilityFamily.SAFE_BLINK, blink.family());
        assertEquals(6D, blink.tuning().range());

        FixedAbilitySpec soulDrain = registry.require("necromancer.signature");
        assertEquals(AbilityFamily.SUMMON, soulDrain.family());
        assertEquals(800, soulDrain.tuning().durationTicks());
        assertTrue(soulDrain.effects().containsAll(Set.of(AbilityEffect.DAMAGE, AbilityEffect.LIFESTEAL)));
        assertTrue(soulDrain.executionTraits().mechanics().contains(AbilityMechanic.REQUIRES_CORPSE));
        assertTrue(!registry.require("lich.signature").executionTraits().mechanics()
                .contains(AbilityMechanic.REQUIRES_CORPSE));
        assertTrue(!registry.require("plaguebringer.signature").executionTraits().mechanics()
                .contains(AbilityMechanic.REQUIRES_CORPSE));

        FixedAbilitySpec guardianEidolon = registry.require("spiritbinder.signature");
        assertEquals(AbilityFamily.SUMMON, guardianEidolon.family());
        assertTrue(guardianEidolon.effects().containsAll(Set.of(
                AbilityEffect.HEAL, AbilityEffect.SHIELD, AbilityEffect.ALLY_PROTECT)));
    }

    @Test
    void spiritcallerCanTriggerItsOwnArmedHealEcho() {
        FixedAbilitySpec echo = registry.require("spiritcaller.signature");
        FixedAbilitySpec link = registry.require("spiritcaller.utility");

        assertTrue(echo.executionTraits().mechanics().contains(AbilityMechanic.HEAL_ECHO));
        assertTrue(link.effects().contains(AbilityEffect.HEAL));
        assertTrue(link.tuning().healingFraction() > 0D);
        assertTrue(echo.resourceCost() + link.resourceCost() <= 100D);
    }

    @Test
    void everyWeakeningAbilityAuthorsItsPercentageDamageMultiplier() {
        for (String abilityId : registry.registeredIds()) {
            FixedAbilitySpec spec = registry.require(abilityId);
            if (spec.effects().contains(AbilityEffect.WEAKEN)) {
                assertTrue(spec.tuning().weakenMultiplier() >= 0D
                                && spec.tuning().weakenMultiplier() < 1D,
                        abilityId + " must author a percentage reduction");
            } else {
                assertEquals(1D, spec.tuning().weakenMultiplier(),
                        abilityId + " must remain neutral without WEAKEN");
            }
        }
    }

    private long count(AbilitySlot slot) {
        return registry.registeredIds().stream()
                .map(registry::require)
                .filter(spec -> spec.slot() == slot)
                .count();
    }
}
