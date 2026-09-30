package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

/** Pins EliteMobs' death handling in instances before the move onto the match core. */
class InstanceDeathTest extends InstanceFixture {
    private TestInstance ongoingWithAlexAndBea() {
        TestInstance instance = instance(1, 4);
        instance.addNewPlayer(alex);
        instance.addNewPlayer(bea);
        ticks(1);
        instance.countdownMatch();
        ticks(41);
        return instance;
    }

    private EntityDamageEvent lethal(PlayerMock player, EntityDamageEvent.DamageCause cause) {
        EntityDamageEvent event = new EntityDamageEvent(player, cause, 1000);
        server.getPluginManager().callEvent(event);
        return event;
    }

    @Test
    void withSpectatorsDisabledEachPlayerHasOneLife() throws Exception {
        setAllowSpectators(false);
        TestInstance instance = instance(1, 4);
        instance.addNewPlayer(alex);
        ticks(1);
        assertEquals(1, instance.getRemainingLives(alex));
    }

    @Test
    void withSpectatorsDisabledADeathRemovesThePlayerToWhereTheyCameFrom() throws Exception {
        setAllowSpectators(false);
        TestInstance instance = ongoingWithAlexAndBea();
        alex.setFireTicks(100);
        alex.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));

        assertTrue(lethal(alex, EntityDamageEvent.DamageCause.CUSTOM).isCancelled());

        assertNull(PlayerData.getMatchInstance(alex));
        assertEquals(overworld.getSpawnLocation(), alex.getLocation());
        assertEquals(0, alex.getFireTicks());
        assertTrue(alex.getActivePotionEffects().isEmpty());
        assertTrue(events.contains("leave:Alex"));
        assertEquals(MatchInstance.InstancedRegionState.ONGOING, instance.getState(), "Bea is still alive");
    }

    @Test
    void theLastPlayerDyingIsADefeatAndRemovesThem() throws Exception {
        setAllowSpectators(false);
        TestInstance instance = ongoingWithAlexAndBea();
        lethal(alex, EntityDamageEvent.DamageCause.CUSTOM);
        lethal(bea, EntityDamageEvent.DamageCause.CUSTOM);
        assertEquals(MatchInstance.InstancedRegionState.COMPLETED_DEFEAT, instance.getState());
        assertNull(PlayerData.getMatchInstance(bea));
        assertTrue(events.contains("end:COMPLETED_DEFEAT"));
    }

    @Test
    void withSpectatorsEnabledTheDeadSpectateWhileOthersFight() {
        TestInstance instance = ongoingWithAlexAndBea();
        instanceWorld.loadChunk(0, 0);
        alex.setLocation(at(instanceWorld, 10.5, 64, 10.5));

        lethal(alex, EntityDamageEvent.DamageCause.CUSTOM);

        assertTrue(instance.isSpectator(alex));
        assertFalse(instance.getPlayers().contains(alex));
        assertTrue(instance.getParticipants().contains(alex));
        assertEquals(GameMode.SPECTATOR, alex.getGameMode());
        assertSame(instance, PlayerData.getMatchInstance(alex));
    }

    @Test
    void voidDamageRescuesInsteadOfKilling() {
        TestInstance instance = ongoingWithAlexAndBea();
        alex.setLocation(at(instanceWorld, 0.5, instanceWorld.getMinHeight() - 70, 0.5));
        assertTrue(lethal(alex, EntityDamageEvent.DamageCause.VOID).isCancelled());
        assertSame(instance, PlayerData.getMatchInstance(alex));
        assertEquals(start(), alex.getLocation());
    }

    @Test
    void lethalDamageBeforeTheStartRemovesThePlayer() {
        TestInstance instance = instance(2, 4);
        instance.addNewPlayer(alex);
        instance.addNewPlayer(bea);
        ticks(1);
        assertTrue(lethal(alex, EntityDamageEvent.DamageCause.CUSTOM).isCancelled());
        assertNull(PlayerData.getMatchInstance(alex));
    }
}
