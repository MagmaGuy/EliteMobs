package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.gamemodes.zoneworld.Grid;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.worldguard.WorldGuardFlagChecker;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by MagmaGuy on 10/10/2016.
 */
public class NaturalEliteMobSpawnEventHandler {

    private static final int range = Bukkit.getServer().getViewDistance() * 16;

    /**
     * This manages Elite Mob that are spawned naturally. It takes a mob that spawns normally in the world, randomizes
     * its chance to become an Elite Mob, scans the area around it for players, finds what combat tier those players,
     * finds if there are additional players, increases the tier of the Elite Mob accordingly and adds sets it as a new
     * Elite Mob
     *
     * @param entity      Entity to check for Elite Mob conversion
     * @param spawnReason Reason for the mob spawning
     */
    public static void naturalMobProcessor(Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {

        if (ValidWorldsConfig.fileConfiguration.getBoolean("Zone-based elitemob spawning worlds." + entity.getWorld().getName())) {
            int eliteMobLevel = (int) (Grid.getMobTierFromLocation(entity.getLocation()) * MobCombatSettingsConfig.perTierLevelIncrease);
            EliteMobEntity eliteMobEntity = new EliteMobEntity((LivingEntity) entity, eliteMobLevel, spawnReason);
            if (spawnReason.equals(CreatureSpawnEvent.SpawnReason.SPAWNER))
                eliteMobEntity.setHasSpecialLoot(false);
            naturalCustomBossSpawn(entity, eliteMobLevel);
            return;
        }

        int eliteMobLevel = getNaturalMobLevel(entity.getLocation());

        //Takes worldguard minimum and maximum level flags into account
        if (EliteMobs.worldguardIsEnabled) {
            Integer minLevel = WorldGuardFlagChecker.getRegionMinimumLevel(entity.getLocation());
            Integer maxLevel = WorldGuardFlagChecker.getRegionMaximumLevel(entity.getLocation());
            if (minLevel != null)
                eliteMobLevel = minLevel > eliteMobLevel ? minLevel : eliteMobLevel;
            if (maxLevel != null)
                eliteMobLevel = maxLevel < eliteMobLevel ? maxLevel : eliteMobLevel;
        }

        if (eliteMobLevel < 0) return;

        if (eliteMobLevel > MobCombatSettingsConfig.naturalElitemobLevelCap)
            eliteMobLevel = MobCombatSettingsConfig.naturalElitemobLevelCap;

        EliteMobEntity eliteMobEntity = new EliteMobEntity((LivingEntity) entity, eliteMobLevel, spawnReason);

        if (spawnReason.equals(CreatureSpawnEvent.SpawnReason.SPAWNER))
            eliteMobEntity.setHasSpecialLoot(false);

        naturalCustomBossSpawn(entity, eliteMobLevel);

    }

    private static void naturalCustomBossSpawn(Entity entity, int eliteMobLevel) {
        /*
        Check to see if they'll become a naturally spawned Custom Boss
         */
        for (CustomBossConfigFields customBossConfigFields : CustomBossConfigFields.getNaturallySpawnedElites())
            if (entity.getType().toString().equalsIgnoreCase(customBossConfigFields.getEntityType()))
                if (ThreadLocalRandom.current().nextDouble() < customBossConfigFields.getSpawnChance()) {
                    CustomBossEntity.constructCustomBoss(customBossConfigFields.getFileName(), entity.getLocation(), eliteMobLevel);
                    return;
                }

    }

    /**
     * This gets the level the natural Elite Mob should have. This level is determined by the power of the armor and weapons
     * the players are wearing, as well as by how many players are in the area.
     *
     * @param spawnLocation Location to scan around for players
     * @return
     */
    public static int getNaturalMobLevel(Location spawnLocation) {

        List<Player> closePlayers = new ArrayList<>();

        for (Player player : spawnLocation.getWorld().getPlayers())
            if (player.getLocation().distanceSquared(spawnLocation) < Math.pow(range, 2))
                if (!player.getGameMode().equals(GameMode.SPECTATOR) && (!player.hasMetadata(MetadataHandler.VANISH_NO_PACKET) ||
                        player.hasMetadata(MetadataHandler.VANISH_NO_PACKET) && !player.getMetadata(MetadataHandler.VANISH_NO_PACKET).get(0).asBoolean()))
                    closePlayers.add(player);

        int eliteMobLevel = 1;
        int playerCount = 0;

        for (Player player : closePlayers) {

            int individualPlayerThreat = MobLevelCalculator.determineMobLevel(player);
            playerCount++;

            if (individualPlayerThreat > eliteMobLevel)
                eliteMobLevel = individualPlayerThreat;

        }

        /*
        Party system modifier
        Each player adds a +0.2 tier bonus
         */
        eliteMobLevel += playerCount * 0.2 * MobTierCalculator.PER_TIER_LEVEL_INCREASE;

        if (MobCombatSettingsConfig.increaseDifficultyWithSpawnDistance) {
            int levelIncrement = SpawnRadiusDifficultyIncrementer.distanceFromSpawnLevelIncrease(spawnLocation);
            eliteMobLevel += levelIncrement;
        }

        if (playerCount == 0 || eliteMobLevel < 1) return 0;

        //add wiggle room
        int wiggle = ThreadLocalRandom.current().nextInt(5) - 2;
        eliteMobLevel = wiggle + eliteMobLevel < 0 ? 0 : eliteMobLevel + wiggle;

        return eliteMobLevel;

    }

}
