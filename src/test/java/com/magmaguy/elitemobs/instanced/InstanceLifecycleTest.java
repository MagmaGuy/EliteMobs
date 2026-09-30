package com.magmaguy.elitemobs.instanced;

import com.magmaguy.elitemobs.api.instanced.MatchJoinEvent;
import com.magmaguy.elitemobs.config.ArenasConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Pins EliteMobs' admission, state sequence and events before the move onto the match core. */
class InstanceLifecycleTest extends InstanceFixture {
    @Test
    void constructionAnnouncesTheInstance() {
        instance(1, 4);
        assertEquals(List.of("instantiate"), events);
    }

    @Test
    void anAdmittedPlayerEntersAtTheStartOnTheNextTick() {
        TestInstance instance = instance(1, 4);
        assertTrue(instance.addNewPlayer(alex));

        assertEquals(overworld, alex.getWorld(), "entry waits one tick");
        assertSame(instance, PlayerData.getMatchInstance(alex));
        assertSame(instance, MatchInstance.getPlayerInstance(alex));
        assertTrue(instance.getPlayers().contains(alex));
        assertTrue(instance.getParticipants().contains(alex));

        ticks(1);
        assertEquals(start(), alex.getLocation());
        assertEquals(3, instance.getRemainingLives(alex));
        assertTrue(instance.isWaitingPlayer(alex));
        assertEquals(List.of("instantiate", "join:Alex"), events);
    }

    @Test
    void theLobbyIsUsedWhileWaiting() {
        TestInstance instance = instance(1, 4);
        instance.lobby(at(instanceWorld, 20.5, 64, 20.5));
        instance.addNewPlayer(alex);
        ticks(1);
        assertEquals(at(instanceWorld, 20.5, 64, 20.5), alex.getLocation());
    }

    @Test
    void aFullInstanceRefusesWithTheArenaFullMessage() {
        TestInstance instance = instance(1, 1);
        assertTrue(instance.addNewPlayer(alex));
        drain(bea);
        assertFalse(instance.addNewPlayer(bea));
        assertEquals(ArenasConfig.getArenaFullMessage(), bea.nextMessage());
        assertNull(PlayerData.getMatchInstance(bea));
    }

    @Test
    void oneCancelledJoinEventRefusesTheWholeGroup() {
        TestInstance instance = instance(1, 4);
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void veto(MatchJoinEvent event) {
                if (event.getPlayer().equals(bea)) event.setCancelled(true);
            }
        }, com.magmaguy.elitemobs.MetadataHandler.PLUGIN);

        assertFalse(InstancePlayerManager.addNewPlayers(List.of(alex, bea), instance));

        assertTrue(instance.getPlayers().isEmpty());
        assertNull(PlayerData.getMatchInstance(alex));
        assertNull(PlayerData.getMatchInstance(bea));
    }

    @Test
    void aGroupIsAdmittedTogether() {
        TestInstance instance = instance(1, 4);
        assertTrue(InstancePlayerManager.addNewPlayers(List.of(alex, bea), instance));
        ticks(1);
        assertEquals(instanceWorld, alex.getWorld());
        assertEquals(instanceWorld, bea.getWorld());
    }

    @Test
    void aPlayerInAnotherInstanceIsRefused() {
        TestInstance first = instance(1, 4);
        TestInstance second = instance(1, 4);
        assertTrue(first.addNewPlayer(alex));
        assertFalse(second.addNewPlayer(alex));
        assertSame(first, PlayerData.getMatchInstance(alex));
    }

    @Test
    void thePermissionIsEnforced() {
        TestInstance instance = instance(1, 4);
        instance.permission("elitemobs.test.instance");
        assertFalse(instance.addNewPlayer(alex));
        alex.addAttachment(com.magmaguy.elitemobs.MetadataHandler.PLUGIN, "elitemobs.test.instance", true);
        assertTrue(instance.addNewPlayer(alex));
    }

    @Test
    void theStateSequenceAndEventsOfAFullRun() {
        TestInstance instance = instance(1, 4);
        instance.addNewPlayer(alex);
        ticks(1);
        assertEquals(MatchInstance.InstancedRegionState.WAITING, instance.getState());

        instance.countdownMatch();
        assertEquals(MatchInstance.InstancedRegionState.STARTING, instance.getState());
        ticks(40);
        assertEquals(MatchInstance.InstancedRegionState.STARTING, instance.getState());
        ticks(1);
        assertEquals(MatchInstance.InstancedRegionState.ONGOING, instance.getState());
        assertEquals(start(), alex.getLocation());

        instance.win();
        assertEquals(MatchInstance.InstancedRegionState.COMPLETED_VICTORY, instance.getState());
        assertTrue(instance.getPlayers().contains(alex), "ending keeps the players inside");

        instance.destroy();
        assertEquals(MatchInstance.InstancedRegionState.WAITING, instance.getState(),
                "a destroyed instance reads WAITING");
        assertEquals(exit(), alex.getLocation());
        assertNull(PlayerData.getMatchInstance(alex));
        assertEquals(List.of("instantiate", "join:Alex", "start", "end:COMPLETED_VICTORY", "leave:Alex", "destroy"),
                events);
    }

    @Test
    void aDestroyedBaseInstanceAcceptsPlayersAgain() {
        TestInstance instance = instance(1, 4);
        instance.addNewPlayer(alex);
        instance.destroy();
        assertFalse(instance.isDefunct());
        assertTrue(instance.isAcceptingNewPlayers());
        assertTrue(instance.addNewPlayer(bea));
    }

    @Test
    void thePlayerSetIsOneLiveObject() {
        TestInstance instance = instance(1, 4);
        var players = instance.getPlayers();
        instance.addNewPlayer(alex);
        instance.addNewPlayer(bea);
        assertSame(players, instance.getPlayers());
        assertTrue(players.containsAll(List.of(alex, bea)));
        instance.removePlayer(bea);
        assertSame(players, instance.getPlayers());
        assertFalse(players.contains(bea));
    }

    @Test
    void removingAPlayerFiresLeaveAndSendsThemToTheExit() {
        TestInstance instance = instance(1, 4);
        instance.addNewPlayer(alex);
        instance.addNewPlayer(bea);
        ticks(1);
        instance.removePlayer(alex);
        assertEquals(exit(), alex.getLocation());
        assertTrue(events.contains("leave:Alex"));
        assertEquals(MatchInstance.InstancedRegionState.WAITING, instance.getState(), "Bea is still in");
    }

    @Test
    void theLastPlayerLeavingAnOngoingInstanceIsADefeat() {
        TestInstance instance = instance(1, 4);
        instance.addNewPlayer(alex);
        ticks(1);
        instance.countdownMatch();
        ticks(41);
        instance.removePlayer(alex);
        assertEquals(MatchInstance.InstancedRegionState.COMPLETED_DEFEAT, instance.getState());
        assertTrue(events.contains("end:COMPLETED_DEFEAT"));
    }

    @Test
    void theLastPlayerLeavingBeforeTheStartEndsWithoutAnEndEvent() {
        TestInstance instance = instance(1, 4);
        instance.addNewPlayer(alex);
        ticks(1);
        instance.removePlayer(alex);
        assertEquals(MatchInstance.InstancedRegionState.COMPLETED_DEFEAT, instance.getState());
        assertTrue(eventsMatching(event -> event.startsWith("end")).isEmpty());
    }

    private static void drain(org.mockbukkit.mockbukkit.entity.PlayerMock player) {
        while (player.nextMessage() != null) {
        }
    }
}
