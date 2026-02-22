package com.magmaguy.elitemobs.treasurechest;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestsConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DynamicDungeonInstance;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WeightedProbability;
import com.magmaguy.magmacore.util.Logger;
import com.magmaguy.magmacore.util.Round;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TreasureChest implements PersistentObject {

    @Getter
    private static final HashMap<Location, TreasureChest> treasureChestHashMap = new HashMap<>();
    private static final ArrayListMultimap<String, TreasureChest> instancedTreasureChests = ArrayListMultimap.create();
    @Getter
    private final CustomTreasureChestConfigFields customTreasureChestConfigFields;
    private final String locationString;
    private final String worldName;
    private final HashSet<UUID> blacklistedPlayersInstance = new HashSet<>();
    @Getter
    private Location location;
    private long restockTime;
    @Getter
    @Setter
    private EMPackage emPackage = null;

    public TreasureChest(CustomTreasureChestConfigFields customTreasureChestConfigFields, String locationString, long restockTime) {
        this.customTreasureChestConfigFields = customTreasureChestConfigFields;
        this.locationString = locationString;
        this.worldName = ConfigurationLocation.worldName(locationString);
        this.location = ConfigurationLocation.serialize(locationString);
        this.restockTime = restockTime;
        this.emPackage = EMPackage.getContent(customTreasureChestConfigFields.getFilename());

        if (!customTreasureChestConfigFields.isEnabled())
            return;

        if (customTreasureChestConfigFields.getChestMaterial() == null)
            return;

        if (!customTreasureChestConfigFields.isInstanced()) {
            initializeChest();
            new PersistentObjectHandler(this);
            treasureChestHashMap.put(location, this);
        } else
            instancedTreasureChests.put(worldName, this);
    }

    public static void initializeInstancedTreasureChests(String instanceWorldName, World instancedWorld) {
        List<TreasureChest> chests = instancedTreasureChests.get(instanceWorldName);
        chests.forEach(treasureChest -> {
            treasureChest.location = ConfigurationLocation.serializeWithInstance(instancedWorld, treasureChest.locationString);
            treasureChest.restockTime = 0;
            new PersistentObjectHandler(treasureChest);
            treasureChest.generateChest();
            treasureChestHashMap.put(treasureChest.location, treasureChest);
        });
    }

    public static void clearTreasureChests() {
        treasureChestHashMap.clear();
    }

    public static void shutdown() {
        treasureChestHashMap.clear();
        instancedTreasureChests.clear();
    }

    public static TreasureChest getTreasureChest(Location location) {
        return getTreasureChestHashMap().get(location);
    }

    private void initializeChest() {
        if (customTreasureChestConfigFields.isInstanced()) return;
        if (location != null && location.getWorld() != null) {
            long time = (restockTime - Instant.now().getEpochSecond()) * 20L;
            if (time < 0)
                generateChest();
            else
                Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, this::generateChest, time);
        }
    }

    private void generateChest() {
        try {
            if (!location.getWorld()
                    .getBlockAt(location).getType().equals(customTreasureChestConfigFields.getChestMaterial()))
                location.getWorld().getBlockAt(location).setType(customTreasureChestConfigFields.getChestMaterial());
        } catch (Exception ex) {
            Logger.warn("Custom Treasure Chest " + customTreasureChestConfigFields.getFilename() + " has an invalid location and can not be placed.");
            return;
        }
        if (location.getBlock().getBlockData() instanceof Directional chest) {
            chest.setFacing(customTreasureChestConfigFields.getFacing());
            location.getBlock().setBlockData(chest);
        } else {
            Logger.warn("Treasure chest " + customTreasureChestConfigFields.getFilename() +
                    " does not have a directional block for the Treasure Chest material " +
                    customTreasureChestConfigFields.getChestMaterial() + " ! Chest materials are directional, is your chest a chest?");
        }
        location.getBlock().getState().update();
    }

    public void doInteraction(Player player) {

        if (customTreasureChestConfigFields.getDropStyle().equals(DropStyle.GROUP))
            if (playerIsInCooldown(player)) {
                if (!customTreasureChestConfigFields.isInstanced())
                    groupTimerCooldownMessage(player, getPlayerCooldown(player));
                return;
            } else if (restockTime > Instant.now().getEpochSecond())
                return;

        // Add player to cooldown BEFORE giving loot to prevent spam clicking exploits
        if (customTreasureChestConfigFields.getDropStyle().equals(DropStyle.GROUP)) {
            if (customTreasureChestConfigFields.isInstanced()) {
                blacklistedPlayersInstance.add(player.getUniqueId());
            } else if (customTreasureChestConfigFields.getRestockTimers() != null) {
                customTreasureChestConfigFields.getRestockTimers().add(cooldownStringConstructor(player));

                // Save the updated restockTimers to the config file
                customTreasureChestConfigFields.getFileConfiguration().set("restockTimers", customTreasureChestConfigFields.getRestockTimers());
                try {
                    customTreasureChestConfigFields.getFileConfiguration().save(customTreasureChestConfigFields.getFile());
                } catch (Exception ex) {
                    Logger.warn("Failed to save restock timers for treasure chest " + customTreasureChestConfigFields.getFilename());
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        customTreasureChestConfigFields.getRestockTimers().removeIf(restockTime -> restockTime.split(":")[0].equals(player.getUniqueId().toString()));

                        // Save the updated restockTimers to the config file after removal
                        customTreasureChestConfigFields.getFileConfiguration().set("restockTimers", customTreasureChestConfigFields.getRestockTimers());
                        try {
                            customTreasureChestConfigFields.getFileConfiguration().save(customTreasureChestConfigFields.getFile());
                        } catch (Exception ex) {
                            Logger.warn("Failed to save restock timers for treasure chest " + customTreasureChestConfigFields.getFilename());
                        }
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 20L * 60 * customTreasureChestConfigFields.getRestockTimer());
            }
        }

        if (ThreadLocalRandom.current().nextDouble() < customTreasureChestConfigFields.getMimicChance()) doMimic(player);
        else doTreasure(player);

        player.playSound(player.getLocation(), SoundsConfig.treasureChestOpenSound, 1, 1);

        if (customTreasureChestConfigFields.getDropStyle().equals(DropStyle.GROUP)) {
            return;
        }

        location.getBlock().setType(Material.AIR);

        restockTime = cooldownTime();
        customTreasureChestConfigFields.setRestockTime(location, restockTime);

        if (!customTreasureChestConfigFields.isInstanced())
            Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, this::generateChest, 20L * 60 * customTreasureChestConfigFields.getRestockTimer());

    }

    private void doMimic(Player player) {
        HashMap<String, Double> weighedValues = new HashMap<>();
        for (String string : this.customTreasureChestConfigFields.getMimicCustomBossesList()) {
            String filename = string.split(":")[0];
            double weight = 1;
            try {
                weight = Double.parseDouble(string.split(":")[1]);
            } catch (Exception ex) {
                weight = 1;
            }
            weighedValues.put(filename, weight);
        }
        CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(WeightedProbability.pickWeighedProbability(weighedValues));
        if (customBossEntity == null) {
            Logger.warn("Failed to spawn mimic for treasure chest " + customTreasureChestConfigFields.getFilename() + ": custom boss config was not found.");
            return;
        }

        Integer dynamicDungeonLevel = getDynamicDungeonLevel(player);
        if (customBossEntity.getCustomBossesConfigFields().getLevel() == -1) {
            // If this is inside a dynamic dungeon instance, match the selected dungeon level.
            if (dynamicDungeonLevel != null) {
                customBossEntity.spawn(location, randomizeLevel(dynamicDungeonLevel), false);
            } else {
                // Outside instances, dynamic mimics should follow the opener's level with slight noise.
                ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
                if (elitePlayerInventory != null) {
                    customBossEntity.spawn(location, randomizeLevel(elitePlayerInventory.getNaturalMobSpawnLevel(false)), false);
                } else {
                    // Fallback for rare edge cases where player inventory data is unavailable.
                    customBossEntity.spawn(location, false);
                }
            }
            return;
        }

        customBossEntity.spawn(location, randomizeTier(), false);
    }

    private void doTreasure(Player player) {
        Integer dynamicDungeonLevel = getDynamicDungeonLevel(player);
        if (dynamicDungeonLevel != null) {
            this.customTreasureChestConfigFields.getCustomLootTable().treasureChestDropAtLevel(player, dynamicDungeonLevel, location);
            return;
        }
        // Outside of instances, scalable chest loot should follow the opener's level.
        if (PlayerData.getMatchInstance(player) == null) {
            ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
            if (elitePlayerInventory != null) {
                int playerLevel = elitePlayerInventory.getNaturalMobSpawnLevel(false);
                this.customTreasureChestConfigFields.getCustomLootTable()
                        .treasureChestDropScalableToPlayerLevel(player, customTreasureChestConfigFields.getChestTier(), playerLevel, location);
                return;
            }
        }
        this.customTreasureChestConfigFields.getCustomLootTable().treasureChestDrop(player, customTreasureChestConfigFields.getChestTier(), location);
    }

    private int randomizeTier() {
        return customTreasureChestConfigFields.getChestTier() * 10 + ThreadLocalRandom.current().nextInt(11);
    }

    private int randomizeLevel(int baseLevel) {
        return Math.max(1, baseLevel + ThreadLocalRandom.current().nextInt(-1, 2));
    }

    private Integer getDynamicDungeonLevel(Player player) {
        if (player != null) {
            MatchInstance matchInstance = PlayerData.getMatchInstance(player);
            if (matchInstance instanceof DynamicDungeonInstance dynamicDungeonInstance)
                return dynamicDungeonInstance.getSelectedLevel();
        }
        // Fallback for edge cases where player data isn't available yet.
        return getDynamicDungeonLevelByWorld();
    }

    private Integer getDynamicDungeonLevelByWorld() {
        if (location == null || location.getWorld() == null) return null;
        for (DungeonInstance dungeonInstance : DungeonInstance.getDungeonInstances()) {
            if (!(dungeonInstance instanceof DynamicDungeonInstance dynamicDungeonInstance)) continue;
            if (dungeonInstance.getWorld() == null) continue;
            if (dungeonInstance.getWorld().equals(location.getWorld())) return dynamicDungeonInstance.getSelectedLevel();
        }
        return null;
    }

    private void lowRankMessage(Player player) {
        // Guild rank removed - message removed
    }

    private void groupTimerCooldownMessage(Player player, long targetTime) {
        player.sendMessage(DefaultConfig.getChestCooldownMessage().replace("$time", timeConverter(targetTime - Instant.now().getEpochSecond())));
    }

    private boolean playerIsInCooldown(Player player) {
        if (customTreasureChestConfigFields.isInstanced())
            return blacklistedPlayersInstance.contains(player.getUniqueId());
        if (customTreasureChestConfigFields.getRestockTimers() == null) return false;
        long now = Instant.now().getEpochSecond();
        boolean saveNeeded = false;
        for (Iterator<String> iterator = customTreasureChestConfigFields.getRestockTimers().iterator(); iterator.hasNext(); ) {
            String string = iterator.next();
            String[] split = string.split(":");
            if (split.length < 2) continue;
            long targetTime;
            try {
                targetTime = Long.parseLong(split[1]);
            } catch (Exception ex) {
                iterator.remove();
                saveNeeded = true;
                continue;
            }
            if (targetTime <= now) {
                iterator.remove();
                saveNeeded = true;
                continue;
            }
            if (split[0].equals(player.getUniqueId().toString())) {
                if (saveNeeded) saveRestockTimers();
                return true;
            }
        }
        if (saveNeeded) saveRestockTimers();
        return false;
    }

    private long getPlayerCooldown(Player player) {
        if (customTreasureChestConfigFields.getRestockTimers() == null) return Instant.now().getEpochSecond();
        for (String string : customTreasureChestConfigFields.getRestockTimers()) {
            String[] split = string.split(":");
            if (split.length < 2) continue;
            if (!split[0].equals(player.getUniqueId().toString())) continue;
            try {
                return Long.parseLong(split[1]);
            } catch (Exception ex) {
                return Instant.now().getEpochSecond();
            }
        }
        return Instant.now().getEpochSecond();
    }

    private String cooldownStringConstructor(Player player) {
        return player.getUniqueId() + ":" + cooldownTime();
    }

    private long cooldownTime() {
        return Instant.now().getEpochSecond() + 60L * this.customTreasureChestConfigFields.getRestockTimer();
    }

    private String timeConverter(long seconds) {
        if (seconds < 0) seconds = 0;
        if (seconds < 60 * 2)
            return seconds + " seconds";
        if (seconds < 60 * 60 * 2)
            return Round.twoDecimalPlaces(seconds / 60D) + "minutes";
        if (seconds < 60 * 60 * 48)
            return Round.twoDecimalPlaces(seconds / 60D / 60) + "hours";
        else
            return Round.twoDecimalPlaces(seconds / 60D / 60 / 48) + "days";
    }

    private void saveRestockTimers() {
        if (customTreasureChestConfigFields.getRestockTimers() == null) return;
        customTreasureChestConfigFields.getFileConfiguration().set("restockTimers", customTreasureChestConfigFields.getRestockTimers());
        try {
            customTreasureChestConfigFields.getFileConfiguration().save(customTreasureChestConfigFields.getFile());
        } catch (Exception ex) {
            Logger.warn("Failed to save restock timers for treasure chest " + customTreasureChestConfigFields.getFilename());
        }
    }

    public void removeTreasureChest() {
        CustomTreasureChestsConfig.removeTreasureChestEntry(location, customTreasureChestConfigFields.getFilename());
        if (location != null && location.getWorld() != null)
            location.getBlock().setBlockData(Material.AIR.createBlockData());
        treasureChestHashMap.remove(location);
    }

    @Override
    public void chunkLoad() {
    }

    @Override
    public void chunkUnload() {
    }

    @Override
    public void worldLoad(World world) {
        this.location = ConfigurationLocation.serialize(locationString);
        initializeChest();
        treasureChestHashMap.put(location, this);
    }

    @Override
    public void worldUnload() {
        treasureChestHashMap.remove(location);
        //todo stop restock timer here
    }

    @Override
    public Location getPersistentLocation() {
        return getLocation();
    }

    @Override
    public String getWorldName() {
        return worldName;
    }

    public enum DropStyle {
        SINGLE,
        GROUP
    }

    public static class TreasureChestEvents implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getClickedBlock() == null) return;
            TreasureChest treasureChest = getTreasureChest(event.getClickedBlock().getLocation());
            if (treasureChest == null) return;
            event.setCancelled(true);
            // Guild rank requirement removed - all players can access chests
            treasureChest.doInteraction(event.getPlayer());
        }

        @EventHandler(ignoreCancelled = true)
        public void onBreak(BlockBreakEvent event) {
            for (TreasureChest treasureChest : treasureChestHashMap.values())
                if (treasureChest.getLocation() != null &&
                        treasureChest.getLocation().getWorld() != null &&
                        event.getBlock().getLocation().equals(treasureChest.location.getBlock().getLocation()))
                    event.setCancelled(true);
        }
    }

}
