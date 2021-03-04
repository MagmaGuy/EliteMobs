package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.gamemodes.zoneworld.Grid;
import com.magmaguy.elitemobs.items.customenchantments.HunterEnchantment;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.utils.PlayerScanner;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM;

/**
 * Created by MagmaGuy on 24/04/2017.
 */
public class NaturalMobSpawnEventHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {

        if (event.getEntity().getType().equals(EntityType.BEE))
            return;

        //This fires for custom bosses, so don't override those spawns
        if (WorldGuardSpawnEventBypasser.isForcedSpawn()) return;

        /*
        Deal with entities spawned within the plugin
         */
        if (EntityTracker.isEliteMob(event.getEntity())) return;

        if (!MobCombatSettingsConfig.doNaturalMobSpawning)
            return;
        if (!ValidWorldsConfig.fileConfiguration.getBoolean("Valid worlds." + event.getEntity().getWorld().getName()))
            return;
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER) &&
                !MobCombatSettingsConfig.doSpawnersSpawnEliteMobs || event.getSpawnReason() == CUSTOM && DefaultConfig.doStrictSpawningRules)
            return;
        if (event.getEntity().getCustomName() != null && DefaultConfig.preventEliteMobConversionOfNamedMobs)
            return;

        if (!EliteMobProperties.isValidEliteMobType(event.getEntityType()))
            return;

        LivingEntity livingEntity = event.getEntity();


        double validChance = MobCombatSettingsConfig.aggressiveMobConversionPercentage;

        ArrayList<Player> nearbyPlayers = PlayerScanner.getNearbyPlayers(livingEntity.getLocation());

        double huntingGearChanceAdder = HunterEnchantment.getHuntingGearBonus(nearbyPlayers);
        validChance += huntingGearChanceAdder;

        if (ValidWorldsConfig.fileConfiguration.getBoolean("Nightmare mode worlds." + event.getEntity().getWorld().getName()))
            validChance += DefaultConfig.nightmareWorldSpawnBonus;

        if (!(ThreadLocalRandom.current().nextDouble() < validChance))
            return;

        if (ValidWorldsConfig.fileConfiguration.getBoolean("Zone-based elitemob spawning worlds." + livingEntity.getWorld().getName())) {
            int eliteMobLevel = (int) (Grid.getMobTierFromLocation(livingEntity.getLocation()));
            EliteMobEntity eliteMobEntity = new EliteMobEntity(livingEntity, eliteMobLevel, event.getSpawnReason());
            if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER))
                eliteMobEntity.setHasSpecialLoot(false);
            naturalCustomBossSpawn(livingEntity, eliteMobLevel);
            return;
        }

        int eliteMobLevel = getNaturalMobLevel(livingEntity.getLocation(), nearbyPlayers);

        //Takes worldguard minimum and maximum level flags into account
        if (EliteMobs.worldguardIsEnabled) {
            Integer minLevel = WorldGuardFlagChecker.getRegionMinimumLevel(livingEntity.getLocation());
            Integer maxLevel = WorldGuardFlagChecker.getRegionMaximumLevel(livingEntity.getLocation());
            if (minLevel != null)
                eliteMobLevel = minLevel > eliteMobLevel ? minLevel : eliteMobLevel;
            if (maxLevel != null)
                eliteMobLevel = maxLevel < eliteMobLevel ? maxLevel : eliteMobLevel;
        }

        if (eliteMobLevel < 0) return;

        if (eliteMobLevel > MobCombatSettingsConfig.naturalElitemobLevelCap)
            eliteMobLevel = MobCombatSettingsConfig.naturalElitemobLevelCap;

        EliteMobEntity eliteMobEntity = new EliteMobEntity(livingEntity, eliteMobLevel, event.getSpawnReason());

        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER))
            eliteMobEntity.setHasSpecialLoot(false);

        naturalCustomBossSpawn(livingEntity, eliteMobLevel);

    }

    private static void naturalCustomBossSpawn(Entity entity, int eliteMobLevel) {
        /*
        Check to see if they'll become a naturally spawned Custom Boss
         */
        if (EliteMobs.worldguardIsEnabled)
            if (!WorldGuardFlagChecker.checkFlag(entity.getLocation(), WorldGuardCompatibility.getEliteMobsEventsFlag()))
                return;

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
    public static int getNaturalMobLevel(Location spawnLocation, ArrayList<Player> nearbyPlayers) {

        int eliteMobLevel = 1;
        int playerCount = 0;

        for (Player player : nearbyPlayers) {
            int individualPlayerThreat = ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getNaturalMobSpawnLevel(true);
            playerCount++;

            if (individualPlayerThreat > eliteMobLevel)
                eliteMobLevel = individualPlayerThreat;
        }

        /*
        Party system modifier
        Each player adds a +2 tier bonus
         */
        eliteMobLevel += playerCount * 2 * MobTierCalculator.PER_TIER_LEVEL_INCREASE;

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
