package com.magmaguy.elitemobs.mobspawning;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.items.customenchantments.HunterEnchantment;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import com.magmaguy.elitemobs.utils.PlayerScanner;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.*;

/**
 * Created by MagmaGuy on 24/04/2017.
 */
public class NaturalMobSpawnEventHandler implements Listener {

    /**
     * This gets the level the natural Elite Mob should have. This level is determined by the power of the armor and weapons
     * the players are wearing, as well as by how many players are in the area.
     *
     * @param spawnLocation Location to scan around for players
     * @return
     */
    public static int getNaturalMobLevel(Location spawnLocation, List<Player> nearbyPlayers) {

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

        if (MobCombatSettingsConfig.isIncreaseDifficultyWithSpawnDistance()) {
            int levelIncrement = SpawnRadiusDifficultyIncrementer.distanceFromSpawnLevelIncrease(spawnLocation);
            eliteMobLevel += levelIncrement;
        }

        if (playerCount == 0 || eliteMobLevel < 1) return 0;

        //add wiggle room
        int wiggle = ThreadLocalRandom.current().nextInt(5) - 2;
        eliteMobLevel = Math.max(wiggle + eliteMobLevel, 1);

        return eliteMobLevel;

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {

        if (event.getSpawnReason().equals(DROWNED) || event.getSpawnReason().equals(BREEDING)) return;

        if (event.getEntity().getType().equals(EntityType.BEE))
            return;


        if (MobPropertiesConfig.getMobProperties().get(event.getEntityType()) == null ||
                !MobPropertiesConfig.getMobProperties().get(event.getEntityType()).isEnabled())
            return;

        if (EliteMobs.worldGuardIsEnabled)
            if (!WorldGuardFlagChecker.checkFlag(event.getLocation(), WorldGuardCompatibility.getELITEMOBS_SPAWN_FLAG()))
                return;

        //This fires for custom bosses, so don't override those spawns
        if (WorldGuardSpawnEventBypasser.isForcedSpawn()) return;

        /*
        Deal with entities spawned within the plugin
         */
        if (EntityTracker.isEliteMob(event.getEntity())) return;

        if (!MobCombatSettingsConfig.isDoNaturalMobSpawning())
            return;
        if (!ValidWorldsConfig.getFileConfiguration().getBoolean("Valid worlds." + event.getEntity().getWorld().getName()))
            return;
        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER) &&
                !MobCombatSettingsConfig.isDoSpawnersSpawnEliteMobs() ||
                event.getSpawnReason() != NATURAL && DefaultConfig.isDoStrictSpawningRules())
            return;
        if (event.getEntity().getCustomName() != null && DefaultConfig.isPreventEliteMobConversionOfNamedMobs())
            return;

        if (!EliteMobProperties.isValidEliteMobType(event.getEntityType()))
            return;

        LivingEntity livingEntity = event.getEntity();


        double validChance = MobCombatSettingsConfig.getAggressiveMobConversionPercentage();

        List<Player> nearbyPlayers = PlayerScanner.getNearbyPlayers(livingEntity.getLocation());

        double huntingGearChanceAdder = HunterEnchantment.getHuntingGearBonus(nearbyPlayers);
        validChance += huntingGearChanceAdder;

        AtomicInteger peacefulModeDebuffs = new AtomicInteger();
        nearbyPlayers.forEach(player -> {
            //Handles situations where fake players got caught in the detection
            if (PlayerData.getPlayerData(player.getUniqueId()) != null && GuildRank.getActiveGuildRank(player) == 0)
                peacefulModeDebuffs.getAndIncrement();
        });

        validChance -= peacefulModeDebuffs.get() * AdventurersGuildConfig.getPeacefulModeEliteChanceDecrease();

        if (ThreadLocalRandom.current().nextDouble() >= validChance) return;

        int eliteMobLevel = getNaturalMobLevel(livingEntity.getLocation(), nearbyPlayers);

        //Takes worldguard minimum and maximum level flags into account
        if (EliteMobs.worldGuardIsEnabled) {
            Integer minLevel = WorldGuardFlagChecker.getRegionMinimumLevel(livingEntity.getLocation());
            Integer maxLevel = WorldGuardFlagChecker.getRegionMaximumLevel(livingEntity.getLocation());
            if (minLevel != null)
                eliteMobLevel = minLevel > eliteMobLevel ? minLevel : eliteMobLevel;
            if (maxLevel != null)
                eliteMobLevel = maxLevel < eliteMobLevel ? maxLevel : eliteMobLevel;
        }

        if (eliteMobLevel < 0) return;

        if (eliteMobLevel > MobCombatSettingsConfig.getNaturalEliteMobLevelCap())
            eliteMobLevel = MobCombatSettingsConfig.getNaturalEliteMobLevelCap();

        EliteEntity eliteEntity = new EliteEntity(livingEntity, eliteMobLevel, event.getSpawnReason());

        if (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER))
            eliteEntity.setEliteLoot(false);

    }

}
