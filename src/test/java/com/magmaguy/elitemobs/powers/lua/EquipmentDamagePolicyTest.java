package com.magmaguy.elitemobs.powers.lua;

import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquipmentDamagePolicyTest {

    @Test
    void exposesOnlyTheSixCombatEquipmentSlots() {
        assertEquals(EquipmentSlot.HAND, EquipmentDamagePolicy.parseSlot("HAND"));
        assertEquals(EquipmentSlot.OFF_HAND, EquipmentDamagePolicy.parseSlot("off_hand"));
        assertEquals(EquipmentSlot.HEAD, EquipmentDamagePolicy.parseSlot("HEAD"));
        assertEquals(EquipmentSlot.CHEST, EquipmentDamagePolicy.parseSlot("CHEST"));
        assertEquals(EquipmentSlot.LEGS, EquipmentDamagePolicy.parseSlot("LEGS"));
        assertEquals(EquipmentSlot.FEET, EquipmentDamagePolicy.parseSlot("FEET"));
        assertThrows(IllegalArgumentException.class,
                () -> EquipmentDamagePolicy.parseSlot("BODY"));
    }

    @Test
    void rejectsNonPositiveAndUnboundedDamage() {
        assertThrows(IllegalArgumentException.class,
                () -> EquipmentDamagePolicy.requestedDamage(0, 0, 100));
        assertThrows(IllegalArgumentException.class,
                () -> EquipmentDamagePolicy.requestedDamage(65, 0, 100));
    }

    @Test
    void returnsExactAppliedDamageAndBreakDecision() {
        assertEquals(3, EquipmentDamagePolicy.requestedDamage(3, 90, 100));
        assertEquals(new EquipmentDamagePolicy.Result(3, 93, false),
                EquipmentDamagePolicy.applyEventDamage(3, 90, 100));
        assertEquals(new EquipmentDamagePolicy.Result(2, 100, true),
                EquipmentDamagePolicy.applyEventDamage(20, 98, 100));
        assertEquals(new EquipmentDamagePolicy.Result(0, 90, false),
                EquipmentDamagePolicy.applyEventDamage(-5, 90, 100));
    }

    @Test
    void cancelledOrInactiveDamageFailsClosed() {
        assertEquals(new EquipmentDamagePolicy.Result(0, 10, false),
                EquipmentDamagePolicy.applyEventDamage(false, true, 3, 10, 100));
        assertEquals(new EquipmentDamagePolicy.Result(0, 10, false),
                EquipmentDamagePolicy.applyEventDamage(true, false, 3, 10, 100));
    }

    @Test
    void emptyNonDamageableAndUnbreakableItemsAreIneligible() {
        assertTrue(!EquipmentDamagePolicy.canDamage(false, true, false, 100));
        assertTrue(!EquipmentDamagePolicy.canDamage(true, false, false, 100));
        assertTrue(!EquipmentDamagePolicy.canDamage(true, true, true, 100));
        assertTrue(EquipmentDamagePolicy.canDamage(true, true, false, 100));
    }

    @Test
    void reportsAlreadyBrokenItemsAsNoOp() {
        assertEquals(0, EquipmentDamagePolicy.requestedDamage(3, 100, 100));
        assertTrue(EquipmentDamagePolicy.applyEventDamage(3, 100, 100).actualDamage() == 0);
    }
}
