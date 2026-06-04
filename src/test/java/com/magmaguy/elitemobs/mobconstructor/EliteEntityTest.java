package com.magmaguy.elitemobs.mobconstructor;

import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EliteEntityTest {

    @Test
    void enderDragonsIgnoreAntiExploitMutations() {
        EliteEntity eliteEntity = new EliteEntity();
        eliteEntity.entityType = EntityType.ENDER_DRAGON;

        eliteEntity.incrementAntiExploit(100, "test");
        eliteEntity.decrementAntiExploit(1);
        eliteEntity.setTriggeredAntiExploit(true);
        eliteEntity.setInAntiExploitCooldown();

        assertTrue(eliteEntity.isEnderDragon());
        assertEquals(0, eliteEntity.antiExploitPoints);
        assertFalse(eliteEntity.isTriggeredAntiExploit());
        assertFalse(eliteEntity.isInAntiExploitCooldown());
        assertTrue(eliteEntity.isEliteLoot());
        assertTrue(eliteEntity.isVanillaLoot());
    }

    @Test
    void nonDragonAntiExploitTriggerStillDisablesLoot() {
        EliteEntity eliteEntity = new EliteEntity();
        eliteEntity.entityType = EntityType.ZOMBIE;

        eliteEntity.setTriggeredAntiExploit(true);

        assertFalse(eliteEntity.isEnderDragon());
        assertTrue(eliteEntity.isTriggeredAntiExploit());
        assertFalse(eliteEntity.isEliteLoot());
        assertFalse(eliteEntity.isVanillaLoot());
    }

}
