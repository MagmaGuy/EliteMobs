package com.magmaguy.elitemobs.combatsystem.combattag;

import com.magmaguy.magmacore.instance.InstanceProtector;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DungeonFoodRegenerationTest {

    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID WORLD_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @AfterEach
    void clearProtectedWorlds() {
        InstanceProtector.shutdown();
    }

    @Test
    void absorptionHeartDamageCountsAsCombat() {
        assertTrue(DungeonFoodRegeneration.hasPositiveDamageBeforeAbsorption(0D, -4D));
    }

    @Test
    void blockedImmuneAndInvalidDamageDoNotCountAsCombat() {
        assertFalse(DungeonFoodRegeneration.hasPositiveDamageBeforeAbsorption(0D, 0D));
        assertFalse(DungeonFoodRegeneration.hasPositiveDamageBeforeAbsorption(0D, 4D));
        assertFalse(DungeonFoodRegeneration.hasPositiveDamageBeforeAbsorption(Double.NaN, 0D));
        assertFalse(DungeonFoodRegeneration.hasPositiveDamageBeforeAbsorption(
                Double.POSITIVE_INFINITY, 0D));
    }

    @Test
    void everyEliteProtectedWorldIsCombatContent() {
        assertTrue(DungeonFoodRegeneration.matchesCombatContent(false, true));
    }

    @Test
    void activeDungeonInstancesRemainCombatContentWithoutWorldProtection() {
        assertTrue(DungeonFoodRegeneration.matchesCombatContent(true, false));
    }

    @Test
    void ordinaryUnprotectedWorldIsNotCombatContent() {
        assertFalse(DungeonFoodRegeneration.matchesCombatContent(false, false));
    }

    @Test
    void protectedWorldEligibilityDoesNotDependOnGameMode() {
        World protectedWorld = world(WORLD_ID);
        InstanceProtector.addProtectedWorld(protectedWorld);

        for (GameMode gameMode : GameMode.values()) {
            assertTrue(
                    DungeonFoodRegeneration.isEligibleDungeonPlayer(
                            player(gameMode, protectedWorld, true, false)),
                    () -> gameMode + " players must be eligible in an EliteMobs-protected world");
        }
    }

    @Test
    void allModesAreEligibleButOnlyVanillaHungerModesReceiveLegacyFoodRegeneration() {
        World protectedWorld = world(WORLD_ID);
        InstanceProtector.addProtectedWorld(protectedWorld);

        assertTrue(DungeonFoodRegeneration.usesVanillaHunger(
                player(GameMode.SURVIVAL, protectedWorld, true, false)));
        assertTrue(DungeonFoodRegeneration.usesVanillaHunger(
                player(GameMode.ADVENTURE, protectedWorld, true, false)));
        assertFalse(DungeonFoodRegeneration.usesVanillaHunger(
                player(GameMode.CREATIVE, protectedWorld, true, false)));
        assertFalse(DungeonFoodRegeneration.usesVanillaHunger(
                player(GameMode.SPECTATOR, protectedWorld, true, false)));
    }

    @Test
    void gameModeDoesNotMakeAnUnprotectedWorldEligible() {
        World ordinaryWorld = world(WORLD_ID);

        for (GameMode gameMode : GameMode.values()) {
            assertFalse(
                    DungeonFoodRegeneration.isEligibleDungeonPlayer(
                            player(gameMode, ordinaryWorld, true, false)),
                    () -> gameMode + " players must remain ineligible in an ordinary world");
        }
    }

    @Test
    void protectedWorldDoesNotMakeOfflineOrDeadPlayersEligible() {
        World protectedWorld = world(WORLD_ID);
        InstanceProtector.addProtectedWorld(protectedWorld);

        assertFalse(DungeonFoodRegeneration.isEligibleDungeonPlayer(
                player(GameMode.CREATIVE, protectedWorld, false, false)));
        assertFalse(DungeonFoodRegeneration.isEligibleDungeonPlayer(
                player(GameMode.SPECTATOR, protectedWorld, true, true)));
    }

    private static Player player(
            GameMode gameMode,
            World world,
            boolean online,
            boolean dead) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "isOnline" -> online;
                    case "isDead" -> dead;
                    case "getGameMode" -> gameMode;
                    case "getUniqueId" -> PLAYER_ID;
                    case "getWorld" -> world;
                    case "getFoodLevel" -> 10;
                    case "hasPotionEffect" -> false;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    case "toString" -> "EligiblePlayer[" + gameMode + "]";
                    default -> throw new UnsupportedOperationException(method.toString());
                });
    }

    private static World world(UUID worldId) {
        return (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getUID" -> worldId;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    case "toString" -> "ProtectedWorld[" + worldId + "]";
                    default -> throw new UnsupportedOperationException(method.toString());
                });
    }
}
