package com.magmaguy.elitemobs.combatsystem.combattag;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CombatParticipantResolverTest {

    @Test
    void resolvesAPlayerWhoDirectlyDamagesAnElite() {
        Player player = entity(Player.class, "Player");
        LivingEntity elite = entity(LivingEntity.class, "Elite");

        assertSame(player, CombatParticipantResolver.resolveEliteCombatPlayer(
                elite, player, candidate -> candidate == elite));
    }

    @Test
    void resolvesAPlayerWhoseProjectileDamagesAnElite() {
        Player player = entity(Player.class, "Player");
        Projectile projectile = projectile(player);
        LivingEntity elite = entity(LivingEntity.class, "Elite");

        assertSame(player, CombatParticipantResolver.resolveEliteCombatPlayer(
                elite, projectile, candidate -> candidate == elite));
    }

    @Test
    void resolvesAPlayerDirectlyDamagedByAnElite() {
        Player player = entity(Player.class, "Player");
        LivingEntity elite = entity(LivingEntity.class, "Elite");

        assertSame(player, CombatParticipantResolver.resolveEliteCombatPlayer(
                player, elite, candidate -> candidate == elite));
    }

    @Test
    void resolvesAPlayerDamagedByAnEliteProjectile() {
        Player player = entity(Player.class, "Player");
        LivingEntity elite = entity(LivingEntity.class, "Elite");
        Projectile projectile = projectile(elite);

        assertSame(player, CombatParticipantResolver.resolveEliteCombatPlayer(
                player, projectile, candidate -> candidate == elite));
    }

    @Test
    void resolvesAPlayerDamagedByEvokerFangsOwnedByAnElite() {
        Player player = entity(Player.class, "Player");
        LivingEntity elite = entity(LivingEntity.class, "Elite");
        EvokerFangs evokerFangs = evokerFangs(elite);

        assertSame(player, CombatParticipantResolver.resolveEliteCombatPlayer(
                player, evokerFangs, candidate -> candidate == elite));
    }

    @Test
    void rejectsDamageThatDoesNotInvolveAnEliteAndAPlayer() {
        Player firstPlayer = entity(Player.class, "FirstPlayer");
        Player secondPlayer = entity(Player.class, "SecondPlayer");
        LivingEntity ordinaryMob = entity(LivingEntity.class, "OrdinaryMob");
        Projectile ordinaryProjectile = projectile(ordinaryMob);
        EvokerFangs ordinaryFangs = evokerFangs(ordinaryMob);
        Predicate<Entity> noElites = ignored -> false;

        assertAll(
                () -> assertNull(CombatParticipantResolver.resolveEliteCombatPlayer(
                        ordinaryMob, firstPlayer, noElites)),
                () -> assertNull(CombatParticipantResolver.resolveEliteCombatPlayer(
                        firstPlayer, ordinaryMob, noElites)),
                () -> assertNull(CombatParticipantResolver.resolveEliteCombatPlayer(
                        firstPlayer, secondPlayer, noElites)),
                () -> assertNull(CombatParticipantResolver.resolveEliteCombatPlayer(
                        firstPlayer, ordinaryProjectile, noElites)),
                () -> assertNull(CombatParticipantResolver.resolveEliteCombatPlayer(
                        firstPlayer, ordinaryFangs, noElites)));
    }

    @Test
    void dungeonCombatIncludesBukkitEnemiesAndPassiveEntityTypeElites() {
        Player player = entity(Player.class, "Player");
        Enemy hostileMob = entity(Enemy.class, "Enemy");
        LivingEntity passiveTypeElite = entity(LivingEntity.class, "PassiveTypeElite");
        Predicate<Entity> passiveEliteOnly = candidate -> candidate == passiveTypeElite;

        assertAll(
                () -> assertSame(player, CombatParticipantResolver.resolveEnemyCombatPlayer(
                        hostileMob, player, ignored -> false)),
                () -> assertSame(player, CombatParticipantResolver.resolveEnemyCombatPlayer(
                        player, hostileMob, ignored -> false)),
                () -> assertSame(player, CombatParticipantResolver.resolveEnemyCombatPlayer(
                        passiveTypeElite, player, passiveEliteOnly)),
                () -> assertSame(player, CombatParticipantResolver.resolveEnemyCombatPlayer(
                        player, passiveTypeElite, passiveEliteOnly)));
    }

    private static Projectile projectile(LivingEntity shooter) {
        return proxy(Projectile.class, "Projectile", shooter);
    }

    private static EvokerFangs evokerFangs(LivingEntity owner) {
        return proxy(EvokerFangs.class, "EvokerFangs", owner);
    }

    private static <T extends Entity> T entity(Class<T> type, String name) {
        return proxy(type, name, null);
    }

    private static <T extends Entity> T proxy(Class<T> type, String name, LivingEntity source) {
        UUID uniqueId = UUID.randomUUID();
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (instance, method, arguments) -> switch (method.getName()) {
                    case "getUniqueId" -> uniqueId;
                    case "getShooter", "getOwner" -> source;
                    case "hashCode" -> System.identityHashCode(instance);
                    case "equals" -> instance == arguments[0];
                    case "toString" -> name + "[" + uniqueId + "]";
                    default -> defaultValue(method.getReturnType());
                }));
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
}
