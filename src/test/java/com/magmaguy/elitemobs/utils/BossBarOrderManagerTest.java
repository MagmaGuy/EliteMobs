package com.magmaguy.elitemobs.utils;

import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossBarOrderManagerTest {

    @AfterEach
    void resetManager() {
        BossBarOrderManager.shutdown();
    }

    @Test
    void exclusiveDialogueSuppressesExistingAndNewBossBarsUntilItCloses() {
        Player player = player();
        BossBarState existingState = new BossBarState();
        BossBar existingBar = bossBar(existingState);
        BossBarState newState = new BossBarState();
        BossBar newBar = bossBar(newState);

        BossBarOrderManager.show(player, existingBar, 1L);
        assertTrue(existingState.players.contains(player));

        BossBarOrderManager.Suspension suspension = BossBarOrderManager.suspendPlayer(player);
        assertFalse(existingState.players.contains(player));

        BossBarOrderManager.show(player, existingBar, 1L);
        assertFalse(existingState.players.contains(player));

        BossBarOrderManager.show(player, newBar, 2L);
        assertFalse(newState.players.contains(player));

        BossBarOrderManager.hide(player, existingBar);
        BossBarOrderManager.resumePlayer(player, suspension);

        assertFalse(existingState.players.contains(player));
        assertTrue(newState.players.contains(player));
    }

    @Test
    void staleDialogueCannotResumeBarsOwnedByItsReplacement() {
        Player player = player();
        BossBarState state = new BossBarState();
        BossBar bossBar = bossBar(state);
        BossBarOrderManager.show(player, bossBar, 1L);

        BossBarOrderManager.Suspension firstDialogue = BossBarOrderManager.suspendPlayer(player);
        BossBarOrderManager.Suspension replacementDialogue = BossBarOrderManager.suspendPlayer(player);

        BossBarOrderManager.resumePlayer(player, firstDialogue);
        assertFalse(state.players.contains(player));
        BossBarOrderManager.resumePlayer(player, firstDialogue);
        assertFalse(state.players.contains(player));

        BossBarOrderManager.resumePlayer(player, replacementDialogue);
        assertTrue(state.players.contains(player));
    }

    @Test
    void finalResumeRestoresCurrentBarsInSortOrder() {
        Player player = player();
        List<String> addOrder = new ArrayList<>();
        BossBar highBar = bossBar(new BossBarState("high", addOrder));
        BossBar lowBar = bossBar(new BossBarState("low", addOrder));
        BossBarOrderManager.show(player, highBar, 20L);
        BossBarOrderManager.show(player, lowBar, 10L);
        BossBarOrderManager.Suspension suspension = BossBarOrderManager.suspendPlayer(player);
        addOrder.clear();

        BossBarOrderManager.resumePlayer(player, suspension);

        assertEquals(List.of("low", "high"), addOrder);
    }

    @Test
    void quitInvalidatesSuspensionSoLateResumeCannotReaddBars() {
        Player player = player();
        BossBarState state = new BossBarState();
        BossBar bossBar = bossBar(state);
        BossBarOrderManager.show(player, bossBar, 1L);
        BossBarOrderManager.Suspension suspension = BossBarOrderManager.suspendPlayer(player);

        BossBarOrderManager.clearPlayer(player);
        BossBarOrderManager.resumePlayer(player, suspension);

        assertFalse(state.players.contains(player));
    }

    @Test
    void quitRemovesPhysicalMembership() {
        Player player = player();
        BossBarState state = new BossBarState();
        BossBar bossBar = bossBar(state);
        BossBarOrderManager.show(player, bossBar, 1L);

        BossBarOrderManager.clearPlayer(player);

        assertFalse(state.players.contains(player));
    }

    @Test
    void shutdownPhysicallyRemovesRegisteredBars() {
        Player player = player();
        BossBarState state = new BossBarState();
        BossBar bossBar = bossBar(state);
        BossBarOrderManager.show(player, bossBar, 1L);

        BossBarOrderManager.shutdown();

        assertFalse(state.players.contains(player));
    }

    @Test
    void failedSuspensionRollsBackItsLeaseAndPhysicalRemovals() {
        Player player = player();
        BossBarState firstState = new BossBarState();
        BossBarState failingState = new BossBarState();
        BossBar firstBar = bossBar(firstState);
        BossBar failingBar = bossBar(failingState);
        BossBarOrderManager.show(player, firstBar, 1L);
        BossBarOrderManager.show(player, failingBar, 2L);
        failingState.throwOnRemove = true;

        assertThrows(IllegalStateException.class, () -> BossBarOrderManager.suspendPlayer(player));
        assertTrue(firstState.players.contains(player));
        assertTrue(failingState.players.contains(player));

        failingState.throwOnRemove = false;
        BossBarOrderManager.Suspension retry = BossBarOrderManager.suspendPlayer(player);
        assertFalse(firstState.players.contains(player));
        assertFalse(failingState.players.contains(player));
        BossBarOrderManager.resumePlayer(player, retry);
        assertTrue(firstState.players.contains(player));
        assertTrue(failingState.players.contains(player));
    }

    @Test
    void terminalDiscardDoesNotRestoreBars() {
        Player player = player();
        BossBarState state = new BossBarState();
        BossBar bossBar = bossBar(state);
        BossBarOrderManager.show(player, bossBar, 1L);
        BossBarOrderManager.Suspension suspension = BossBarOrderManager.suspendPlayer(player);

        BossBarOrderManager.discardSuspension(player, suspension);

        assertFalse(state.players.contains(player));
    }

    @Test
    void failedResumeRollsBackAndKeepsItsLeaseRetryable() {
        Player player = player();
        BossBarState firstState = new BossBarState();
        BossBarState failingState = new BossBarState();
        BossBar firstBar = bossBar(firstState);
        BossBar failingBar = bossBar(failingState);
        BossBarOrderManager.show(player, firstBar, 1L);
        BossBarOrderManager.show(player, failingBar, 2L);
        BossBarOrderManager.Suspension suspension = BossBarOrderManager.suspendPlayer(player);
        failingState.failedAddsRemaining = 1;

        assertThrows(IllegalStateException.class, () -> BossBarOrderManager.resumePlayer(player, suspension));
        assertFalse(firstState.players.contains(player));
        assertFalse(failingState.players.contains(player));

        BossBarOrderManager.resumePlayer(player, suspension);
        assertTrue(firstState.players.contains(player));
        assertTrue(failingState.players.contains(player));
    }

    @Test
    void quitCleanupContinuesAfterFailureAndInvalidatesSuspension() {
        Player player = player();
        BossBarState failingState = new BossBarState();
        BossBarState laterState = new BossBarState();
        BossBar failingBar = bossBar(failingState);
        BossBar laterBar = bossBar(laterState);
        BossBarOrderManager.show(player, failingBar, 1L);
        BossBarOrderManager.show(player, laterBar, 2L);
        BossBarOrderManager.suspendPlayer(player);
        failingState.throwOnRemove = true;
        int laterRemovalsBeforeCleanup = laterState.removeCalls;

        assertThrows(IllegalStateException.class, () -> BossBarOrderManager.clearPlayer(player));

        assertEquals(laterRemovalsBeforeCleanup + 1, laterState.removeCalls);
        failingState.throwOnRemove = false;
        BossBarOrderManager.show(player, laterBar, 2L);
        assertTrue(laterState.players.contains(player));
    }

    @Test
    void shutdownContinuesAfterFailureAndClearsManagerState() {
        Player player = player();
        BossBarState failingState = new BossBarState();
        BossBarState laterState = new BossBarState();
        BossBar failingBar = bossBar(failingState);
        BossBar laterBar = bossBar(laterState);
        BossBarOrderManager.show(player, failingBar, 1L);
        BossBarOrderManager.show(player, laterBar, 2L);
        failingState.throwOnRemoveAll = true;
        int laterRemovalsBeforeShutdown = laterState.removeAllCalls;

        assertThrows(IllegalStateException.class, BossBarOrderManager::shutdown);

        assertEquals(laterRemovalsBeforeShutdown + 1, laterState.removeAllCalls);
        assertEquals(1, failingState.removeAllCalls);
        assertFalse(laterState.players.contains(player));
        failingState.throwOnRemoveAll = false;
        int failingRemovalsAfterShutdown = failingState.removeAllCalls;
        int laterRemovalsAfterShutdown = laterState.removeAllCalls;
        BossBarOrderManager.shutdown();
        assertEquals(failingRemovalsAfterShutdown, failingState.removeAllCalls);
        assertEquals(laterRemovalsAfterShutdown, laterState.removeAllCalls);
    }

    private static Player player() {
        UUID uniqueId = UUID.randomUUID();
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getUniqueId" -> uniqueId;
                    case "isOnline", "isValid" -> true;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    case "toString" -> "TestPlayer[" + uniqueId + "]";
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static BossBar bossBar(BossBarState state) {
        return (BossBar) Proxy.newProxyInstance(
                BossBar.class.getClassLoader(),
                new Class<?>[]{BossBar.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "addPlayer" -> {
                        if (state.failedAddsRemaining > 0) {
                            state.failedAddsRemaining--;
                            throw new IllegalStateException("add failed");
                        }
                        state.players.add((Player) arguments[0]);
                        if (state.addOrder != null) state.addOrder.add(state.name);
                        yield null;
                    }
                    case "removePlayer" -> {
                        state.removeCalls++;
                        if (state.throwOnRemove) throw new IllegalStateException("remove failed");
                        state.players.remove((Player) arguments[0]);
                        yield null;
                    }
                    case "removeAll" -> {
                        state.removeAllCalls++;
                        if (state.throwOnRemoveAll) throw new IllegalStateException("remove all failed");
                        state.players.clear();
                        yield null;
                    }
                    case "getPlayers" -> List.copyOf(state.players);
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    case "toString" -> "TestBossBar";
                    default -> defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        throw new IllegalArgumentException("Unsupported primitive: " + type);
    }

    private static final class BossBarState {
        private final Set<Player> players = new HashSet<>();
        private final String name;
        private final List<String> addOrder;
        private boolean throwOnRemove;
        private boolean throwOnRemoveAll;
        private int failedAddsRemaining;
        private int removeCalls;
        private int removeAllCalls;

        private BossBarState() {
            this(null, null);
        }

        private BossBarState(String name, List<String> addOrder) {
            this.name = name;
            this.addOrder = addOrder;
        }
    }
}
