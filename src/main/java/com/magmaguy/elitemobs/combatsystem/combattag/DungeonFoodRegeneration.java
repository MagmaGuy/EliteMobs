package com.magmaguy.elitemobs.combatsystem.combattag;

import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Pure policy helpers for legacy dungeon food regeneration.
 *
 * <p>{@link DungeonCombatRuntime} owns event parsing, combat time, and scheduling. Keeping the
 * effect policy separate makes it possible for Experimental Combat to replace legacy hunger
 * behavior without creating a second combat tracker.</p>
 */
public final class DungeonFoodRegeneration {

    static final int SATURATION_DURATION_TICKS = 1;
    static final int SATURATION_AMPLIFIER = 0;

    private DungeonFoodRegeneration() {
    }

    static boolean shouldRegenerateFood(Player player, boolean inCombat) {
        return !inCombat &&
                usesVanillaHunger(player) &&
                isEligibleDungeonPlayer(player) &&
                player.getFoodLevel() < 20 &&
                !player.hasPotionEffect(PotionEffectType.SATURATION);
    }

    static boolean usesVanillaHunger(Player player) {
        GameMode gameMode = player.getGameMode();
        return gameMode == GameMode.SURVIVAL || gameMode == GameMode.ADVENTURE;
    }

    static boolean hasPositiveDamageBeforeAbsorption(
            double finalDamage,
            double absorptionModifier) {
        double damageBeforeAbsorption = finalDamage - absorptionModifier;
        return Double.isFinite(damageBeforeAbsorption) && damageBeforeAbsorption > 0D;
    }

    static boolean isEligibleDungeonPlayer(Player player) {
        if (!player.isOnline() || player.isDead()) return false;
        return isInEligibleCombatContent(player);
    }

    static boolean isInEligibleCombatContent(Player player) {
        return matchesCombatContent(
                PlayerData.getMatchInstance(player) instanceof DungeonInstance dungeon
                        && player.getWorld().equals(dungeon.getWorld()),
                EliteMobsWorld.isEliteMobsWorld(player.getWorld().getUID()));
    }

    static boolean matchesCombatContent(boolean activeDungeonInstance, boolean eliteProtectedWorld) {
        return activeDungeonInstance || eliteProtectedWorld;
    }

    static PotionEffect createFoodRegenerationEffect() {
        return new PotionEffect(
                PotionEffectType.SATURATION,
                SATURATION_DURATION_TICKS,
                SATURATION_AMPLIFIER,
                false,
                false,
                false);
    }

}
