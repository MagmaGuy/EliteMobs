package com.magmaguy.elitemobs.powers.scripts;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.ElitePowerPauseReason;
import com.magmaguy.elitemobs.powers.scripts.caching.EliteScriptBlueprint;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class EliteScriptRuntimeClosureTest {
    private ServerMock server;
    private JavaPlugin plugin;
    private PlayerMock player;
    private EliteEntity actor;
    private final List<EliteScript> scripts = new ArrayList<>();

    @BeforeEach
    void openServer() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        MetadataHandler.PLUGIN = plugin;
        var world = server.addSimpleWorld("phase-room");
        player = server.addPlayer();
        player.teleport(new Location(world, 0, 65, 0));
        var body = world.spawn(new Location(world, 1, 65, 0), Zombie.class);
        actor = spy(new EliteEntity());
        doReturn(body).when(actor).getLivingEntity();
        doReturn(body).when(actor).getUnsyncedLivingEntity();
        doReturn(body.getLocation()).when(actor).getLocation();
        doReturn("Phase boss").when(actor).getName();
        doReturn(true).when(actor).isValid();
    }

    @AfterEach
    void closeServer() {
        scripts.forEach(EliteScript::closeRuntime);
        EliteScriptBlueprint.shutdown();
        MockBukkit.unmock();
        MetadataHandler.PLUGIN = null;
    }

    @Test
    void closingBeforeDelayExpiresPreventsTheActionAndReleasesItsTask() {
        EliteScript script = script("Delayed", message("old phase", 5, 0, 1), new LinkedHashMap<>());
        trigger(script);
        script.closeRuntime();

        assertEquals(0, pendingTasks(), "closing a runtime must cancel its pending delay immediately");
        server.getScheduler().performTicks(10);
        assertNull(player.nextMessage());
    }

    @Test
    void closingAnActiveFiniteRepeatStopsFurtherPulsesEvenWhenActorIsStillValid() {
        EliteScript script = script("Repeated", message("pulse", 0, 2, 5), new LinkedHashMap<>());
        trigger(script);
        server.getScheduler().performOneTick();
        assertEquals("pulse", player.nextMessage());

        script.closeRuntime();
        assertTrue(actor.isValid(), "a phase switch keeps the logical actor valid");
        server.getScheduler().performTicks(20);
        assertNull(player.nextMessage(), "old phase pulses must not reach the next phase");
        assertEquals(0, pendingTasks());
    }

    @Test
    void closingWhileSuppressedCancelsAnInfiniteRepeatInsteadOfLeavingAParkedTask() {
        EliteScript script = script("Infinite", message("pulse", 0, 2, -1), new LinkedHashMap<>());
        trigger(script);
        server.getScheduler().performOneTick();
        assertEquals("pulse", player.nextMessage());
        var pause = actor.getPowerSuppression().acquire(ElitePowerPauseReason.INTERRUPT);
        server.getScheduler().performTicks(4);

        script.closeRuntime();
        assertEquals(0, pendingTasks(), "paused callbacks must still be released on close");
        pause.close();
        server.getScheduler().performTicks(10);
        assertNull(player.nextMessage());
    }

    @Test
    void replacingAWholePhaseCancelsNestedLaunchersAndPulsesButAllowsTheNewPhase() {
        Map<String, ScriptExecutable> oldPhase = new LinkedHashMap<>();
        EliteScript pulse = script("DamageCylinder", message("old pulse", 2, 2, 4), oldPhase);
        EliteScript launcher = script("StartEffect", Map.of(
                "action", "RUN_SCRIPT", "repeatEvery", 6, "times", 7,
                "scripts", List.of("DamageCylinder")), oldPhase);
        trigger(launcher);
        server.getScheduler().performTicks(4);
        assertEquals("old pulse", player.nextMessage(), "the nested action must have started");
        while (player.nextMessage() != null) { }

        // This is the phase replacement seam: every old power closes, while its logical actor survives.
        oldPhase.values().forEach(value -> ((EliteScript) value).closeRuntime());
        actor.getPowerSuppression().close();
        assertFalse(actor.getPowerSuppression().isSuppressed());
        EliteScript replacement = script("NewPhase", message("new phase", 1, 0, 1), new LinkedHashMap<>());
        trigger(replacement);
        server.getScheduler().performTicks(60);

        assertEquals("new phase", player.nextMessage());
        assertNull(player.nextMessage(), "closed launchers must not create new child work");
        assertEquals(0, pendingTasks());
    }

    @Test
    void closedRuntimeRejectsLaterEventsAndRepeatedCloseIsSafe() {
        EliteScript script = script("Closed", message("should not run", 0, 0, 1), new LinkedHashMap<>());
        script.closeRuntime();
        script.closeRuntime();
        trigger(script);

        assertNull(player.nextMessage());
        assertEquals(0, pendingTasks());
    }

    @Test
    void activeFiniteActionRetainsItsDelayAndExactRepeatCount() {
        EliteScript script = script("Normal", message("pulse", 3, 2, 3), new LinkedHashMap<>());
        trigger(script);
        server.getScheduler().performTicks(2);
        assertNull(player.nextMessage());
        server.getScheduler().performTicks(12);

        assertEquals(List.of("pulse", "pulse", "pulse"), drainMessages());
        assertEquals(0, pendingTasks(), "completed action tasks should not remain scheduled");
    }

    @Test
    void explicitRuntimeResumeAcceptsNewActionsWithoutRevivingOldCallbacks() {
        EliteScript script = script("Resumed", message("pulse", 3, 2, 3), new LinkedHashMap<>());
        trigger(script);
        script.closeRuntime();

        // The existing persistence/native-body resume path invokes this hook on retained powers.
        script.initializeCustomEvents(actor);
        trigger(script);
        server.getScheduler().performTicks(15);

        assertEquals(List.of("pulse", "pulse", "pulse"), drainMessages());
        assertEquals(0, pendingTasks());
    }

    @Test
    void admittedFiniteDeathChainFinishesAfterTheBossIsRemoved() {
        Map<String, ScriptExecutable> phase = new LinkedHashMap<>();
        script("AwakenGarg", message("A Gargoyle has awakened!", 2, 0, 1), phase);
        EliteScript trigger = script("DeathTrigger", Map.of(
                "action", "RUN_SCRIPT", "wait", 2, "scripts", List.of("AwakenGarg")),
                phase, "EliteMobDeathEvent");
        trigger.check(new EliteMobDeathEvent(actor), actor, player);
        EliteScript combat = script("Combat", message("old combat", 3, 2, 3), phase);
        trigger(combat);

        // EliteMobDeathEvent dispatches before remove(DEATH) closes every power on the dead boss.
        phase.values().forEach(value -> ((EliteScript) value).closeRuntime(RemovalReason.DEATH));
        doReturn(false).when(actor).isValid();
        server.getScheduler().performTicks(10);

        assertEquals(List.of("A Gargoyle has awakened!"), drainMessages());
        assertEquals(0, pendingTasks());
    }

    @Test
    void deathClosureRejectsFreshEventsAndCancelsInfiniteDeathActions() {
        Map<String, ScriptExecutable> phase = new LinkedHashMap<>();
        EliteScript infinite = script("InfiniteDeath", message("loop", 2, 2, -1), phase,
                "EliteMobDeathEvent");
        EliteScript direct = script("DeathMessage", message("fresh event", 0, 0, 1), phase,
                "EliteMobDeathEvent");
        infinite.check(new EliteMobDeathEvent(actor), actor, player);
        phase.values().forEach(value -> ((EliteScript) value).closeRuntime(RemovalReason.DEATH));
        direct.check(new EliteMobDeathEvent(actor), actor, player);
        server.getScheduler().performTicks(10);

        assertNull(player.nextMessage());
        assertEquals(0, pendingTasks());
    }

    @Test
    void hardCloseCancelsAnAdmittedDeathContinuation() {
        EliteScript script = script("DeathMessage", message("must stop", 3, 0, 1),
                new LinkedHashMap<>(), "EliteMobDeathEvent");
        script.check(new EliteMobDeathEvent(actor), actor, player);
        script.closeRuntime(RemovalReason.DEATH);
        script.closeRuntime();
        server.getScheduler().performTicks(10);

        assertNull(player.nextMessage());
        assertEquals(0, pendingTasks());
    }

    @Test
    void explicitResumeCancelsOldDeathContinuationsBeforeAcceptingNewEvents() {
        EliteScript script = script("Resumed", message("pulse", 3, 0, 1),
                new LinkedHashMap<>(), "EliteMobDeathEvent");
        script.check(new EliteMobDeathEvent(actor), actor, player);
        script.closeRuntime(RemovalReason.DEATH);
        script.initializeCustomEvents(actor);
        script.check(new EliteMobDeathEvent(actor), actor, player);
        server.getScheduler().performTicks(10);

        assertEquals(List.of("pulse"), drainMessages());
        assertEquals(0, pendingTasks());
    }

    @Test
    void deathContinuationStopsWhenItsWorldIsNoLongerLoaded() {
        EliteScript script = script("DeathMessage", message("unloaded world", 3, 0, 1),
                new LinkedHashMap<>(), "EliteMobDeathEvent");
        script.check(new EliteMobDeathEvent(actor), actor, player);
        script.closeRuntime(RemovalReason.DEATH);

        // A removed boss is no longer in tracking to receive a later world-unload close.
        World unloadedWorld = mock(World.class);
        doReturn(UUID.randomUUID()).when(unloadedWorld).getUID();
        doReturn("unloaded-phase-room").when(unloadedWorld).getName();
        doReturn(new Location(unloadedWorld, 1, 65, 0)).when(actor).getLocation();
        server.getScheduler().performTicks(10);

        assertNull(player.nextMessage());
        assertEquals(0, pendingTasks());
    }

    @Test
    void aRecursiveDeathChainCannotKeepAClosedRuntimeAlive() {
        Map<String, ScriptExecutable> phase = new LinkedHashMap<>();
        Map<String, Object> loopAction = new LinkedHashMap<>(message("aftermath", 1, 0, 1));
        loopAction.put("scripts", List.of("Loop"));
        script("Loop", loopAction, phase);
        EliteScript trigger = script("DeathTrigger", Map.of(
                "action", "RUN_SCRIPT", "wait", 2, "scripts", List.of("Loop")),
                phase, "EliteMobDeathEvent");
        trigger.check(new EliteMobDeathEvent(actor), actor, player);
        phase.values().forEach(value -> ((EliteScript) value).closeRuntime(RemovalReason.DEATH));
        server.getScheduler().performTicks(12);

        assertEquals(List.of("aftermath"), drainMessages());
        assertEquals(0, pendingTasks());
    }

    @Test
    void activeScriptCyclesRetainTheirAuthoredBehaviorUntilClosed() {
        Map<String, ScriptExecutable> phase = new LinkedHashMap<>();
        Map<String, Object> loopAction = new LinkedHashMap<>(message("active loop", 1, 0, 1));
        loopAction.put("scripts", List.of("Loop"));
        EliteScript loop = script("Loop", loopAction, phase);
        trigger(loop);
        server.getScheduler().performTicks(6);
        assertTrue(drainMessages().size() >= 3);

        loop.closeRuntime();
        server.getScheduler().performTicks(6);
        assertNull(player.nextMessage());
        assertEquals(0, pendingTasks());
    }

    @Test
    void aLandingCallbackFromBeforeResumeCannotDispatchIntoTheNewActions() {
        Map<String, ScriptExecutable> phase = new LinkedHashMap<>();
        script("Landing", message("landed", 0, 0, 1), phase);
        EliteScript projectile = script("Projectile", Map.of(
                "action", "SUMMON_ENTITY", "entityType", "ARROW",
                "landingScripts", List.of("Landing")), phase);
        FallingEntityDataPair oldLanding = landing(projectile);
        ScriptListener.runEvent(oldLanding, player.getLocation());
        assertEquals("landed", player.nextMessage());

        phase.values().forEach(value -> ((EliteScript) value).closeRuntime());
        phase.values().forEach(value -> ((EliteScript) value).initializeCustomEvents(actor));
        ScriptListener.runEvent(oldLanding, player.getLocation());
        assertNull(player.nextMessage(), "the landing still belongs to the closed action");

        ScriptListener.runEvent(landing(projectile), player.getLocation());
        assertEquals("landed", player.nextMessage());
    }

    private FallingEntityDataPair landing(EliteScript script) {
        ScriptAction action = script.scriptActions.getScriptActionsList().getFirst();
        ScriptActionData data = new ScriptActionData(actor, player,
                new ScriptTargets(action.getBlueprint().getScriptTargets(), script),
                script.getScriptZone(), new EliteMobSpawnEvent(actor));
        return new FallingEntityDataPair(action, data);
    }

    private EliteScript script(String name, Map<String, Object> action, Map<String, ScriptExecutable> phase) {
        return script(name, action, phase, "EliteMobSpawnEvent");
    }

    private EliteScript script(String name, Map<String, Object> action,
                               Map<String, ScriptExecutable> phase, String event) {
        var config = new CustomConfigFields("runtime_closure.yml", true);
        var blueprint = new EliteScriptBlueprint(config, Map.of(
                "Events", List.of(event), "Actions", List.of(action)), name);
        var script = new EliteScript(blueprint, phase, actor);
        scripts.add(script);
        return script;
    }

    private void trigger(EliteScript script) {
        script.check(new EliteMobSpawnEvent(actor), actor, player);
    }

    private static Map<String, Object> message(String text, int wait, int repeat, int times) {
        return Map.of("action", "MESSAGE", "sValue", text, "wait", wait,
                "repeatEvery", repeat, "times", times, "Target", Map.of("targetType", "DIRECT_TARGET"));
    }

    private long pendingTasks() {
        return server.getScheduler().getPendingTasks().stream().filter(task -> task.getOwner() == plugin).count();
    }

    private List<String> drainMessages() {
        List<String> messages = new ArrayList<>();
        String message;
        while ((message = player.nextMessage()) != null) messages.add(message);
        return messages;
    }
}
