package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins EliteMobs' teleport guard, movement API, intruder ejection and void rescue. Strangers
 * teleporting into a waiting dungeon world is deliberately not pinned: the match core refuses it.
 */
class InstanceMovementTest extends InstanceFixture {
    private TestInstance ongoing() {
        TestInstance instance = instance(1, 4);
        instance.addNewPlayer(alex);
        ticks(1);
        instance.countdownMatch();
        ticks(41);
        assertEquals(MatchInstance.InstancedRegionState.ONGOING, instance.getState());
        return instance;
    }

    @Test
    void participantsCannotTeleportOutOnTheirOwn() {
        ongoing();
        assertFalse(alex.teleport(overworld.getSpawnLocation()));
        assertEquals(instanceWorld, alex.getWorld());
    }

    @Test
    void enderPearlsInsideTheInstanceAreBlocked() {
        ongoing();
        assertFalse(alex.teleport(at(instanceWorld, 5, 64, 5), TeleportCause.ENDER_PEARL));
    }

    @Test
    void aWaitingParticipantAlreadyInsideCannotTeleportOut() {
        TestInstance instance = instance(2, 4);
        instance.addNewPlayer(alex);
        ticks(1);
        assertFalse(alex.teleport(overworld.getSpawnLocation()));
        assertSame(instance, PlayerData.getMatchInstance(alex));
    }

    @Test
    void aWaitingParticipantTeleportingElsewhereBeforeEntryIsRemoved() {
        TestInstance instance = instance(2, 4);
        instance.addNewPlayer(alex);
        assertTrue(alex.teleport(at(overworld, 40, 64, 40)));
        assertNull(PlayerData.getMatchInstance(alex));
        assertFalse(instance.getPlayers().contains(alex));
    }

    @Test
    void teleportWithinWorldMovesAnActiveParticipantInsideTheInstance() {
        ongoing();
        assertTrue(InstancePlayerMovement.teleportWithinWorld(alex, at(instanceWorld, 7.5, 64, 7.5), TeleportCause.PLUGIN));
        assertEquals(at(instanceWorld, 7.5, 64, 7.5), alex.getLocation());
    }

    @Test
    void teleportWithinWorldRefusesOtherWorlds() {
        ongoing();
        assertFalse(InstancePlayerMovement.teleportWithinWorld(alex, overworld.getSpawnLocation(), TeleportCause.PLUGIN));
    }

    @Test
    void teleportLeavingInstanceRemovesTheParticipantOnceTheyAreOut() {
        TestInstance instance = ongoing();
        instance.addNewPlayer(bea);
        assertTrue(InstancePlayerMovement.teleportLeavingInstance(alex, at(overworld, 30.5, 64, 30.5), TeleportCause.COMMAND));
        assertEquals(at(overworld, 30.5, 64, 30.5), alex.getLocation());
        assertNull(PlayerData.getMatchInstance(alex));
        assertTrue(events.contains("leave:Alex"));
    }

    @Test
    void teleportLeavingInstanceRefusesDestinationsInsideIt() {
        ongoing();
        assertFalse(InstancePlayerMovement.teleportLeavingInstance(alex, at(instanceWorld, 3, 64, 3), TeleportCause.COMMAND));
    }

    @Test
    void staffWithTheWithinPermissionMayUseCommandTeleportsInsideTheInstance() {
        ongoing();
        alex.addAttachment(MetadataHandler.PLUGIN, "elitemobs.instanced.teleport.within", true);
        assertTrue(alex.teleport(at(instanceWorld, 9.5, 64, 9.5), TeleportCause.COMMAND));
        assertFalse(alex.teleport(at(instanceWorld, 10.5, 64, 10.5), TeleportCause.ENDER_PEARL));
        assertFalse(alex.teleport(overworld.getSpawnLocation(), TeleportCause.COMMAND));
    }

    @Test
    void intrudersAreEjectedToTheExitWhileTheInstanceRuns() {
        ongoing();
        stranger.setLocation(at(instanceWorld, 4, 64, 4));
        ticks(1);
        assertEquals(exit(), stranger.getLocation());
    }

    @Test
    void holdersOfEliteMobsStarAreNotEjected() {
        ongoing();
        stranger.addAttachment(MetadataHandler.PLUGIN, "elitemobs.*", true);
        stranger.setLocation(at(instanceWorld, 4, 64, 4));
        ticks(1);
        assertEquals(instanceWorld, stranger.getWorld());
    }

    @Test
    void intrudersAreLeftAloneWhileWaiting() {
        instance(2, 4).addNewPlayer(alex);
        stranger.setLocation(at(instanceWorld, 4, 64, 4));
        ticks(1);
        assertEquals(instanceWorld, stranger.getWorld());
    }

    @Test
    void playersBelowTheWorldAreRescuedToTheStartWithoutASafeSpot() {
        ongoing();
        alex.setFallDistance(30);
        alex.setLocation(at(instanceWorld, 0.5, instanceWorld.getMinHeight() - 5, 0.5));
        ticks(1);
        assertEquals(start(), alex.getLocation());
        assertEquals(0f, alex.getFallDistance());
    }

    @Test
    void rescueReturnsToTheLastSolidBlockThenTheStartAfterThreeFailures() {
        ongoing();
        alex.setOnGround(true);
        alex.setLocation(at(instanceWorld, 12.5, 70, 12.5));
        ticks(1);
        alex.setOnGround(false);
        for (int rescue = 1; rescue <= 3; rescue++) {
            alex.setLocation(at(instanceWorld, 12.5, instanceWorld.getMinHeight() - 5, 12.5));
            ticks(1);
            assertEquals(at(instanceWorld, 12.5, 70, 12.5), alex.getLocation(), "rescue " + rescue);
        }
        alex.setLocation(at(instanceWorld, 12.5, instanceWorld.getMinHeight() - 5, 12.5));
        ticks(1);
        assertEquals(start(), alex.getLocation());
    }
}
