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
import org.bukkit.scheduler.BukkitTask;

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
    private BukkitTask restockTask;
    private PersistentObjectHandler persistentObjectHandler;
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
            registerPersistentHandler();
            registerChest();
            scheduleRestock();
        } else
            instancedTreasureChests.put(worldName, this);
    }

    /**
     * Creates one runtime chest for one dungeon world from an immutable
     * instanced blueprint. Runtime state (location, blacklist, handler, and
     * restock task) must never be shared between simultaneous instances.
     */
    private TreasureChest(TreasureChest blueprint, World instancedWorld) {
        this.customTreasureChestConfigFields = blueprint.customTreasureChestConfigFields;
        this.locationString = blueprint.locationString;
        this.worldName = blueprint.worldName;
        this.location = ConfigurationLocation.serializeWithInstance(instancedWorld, locationString);
        this.restockTime = 0;
        this.emPackage = blueprint.emPackage;

        if (!hasValidConfiguration() || location == null) return;
        registerPersistentHandler();
        registerChest();
        scheduleRestock();
    }

    public static void initializeInstancedTreasureChests(String instanceWorldName, World instancedWorld) {
        List<TreasureChest> chests = instancedTreasureChests.get(instanceWorldName);
        chests.forEach(blueprint -> new TreasureChest(blueprint, instancedWorld));
    }

    public static void clearTreasureChests() {
        Set<TreasureChest> activeChests = Collections.newSetFromMap(new IdentityHashMap<>());
        activeChests.addAll(treasureChestHashMap.values());
        activeChests.forEach(treasureChest -> {
            treasureChest.cancelRestock();
            treasureChest.unregisterPersistentHandler();
        });
        treasureChestHashMap.clear();
    }

    public static void removeInstancedTreasureChests(World world) {
        if (world == null) return;
        UUID worldUUID = world.getUID();
        Set<TreasureChest> removedChests = Collections.newSetFromMap(new IdentityHashMap<>());
        treasureChestHashMap.entrySet().removeIf(entry -> {
            Location keyLocation = entry.getKey();
            Location chestLocation = entry.getValue().location;
            boolean remove = isLocationInWorld(keyLocation, worldUUID) || isLocationInWorld(chestLocation, worldUUID);
            if (remove) removedChests.add(entry.getValue());
            return remove;
        });
        instancedTreasureChests.values().forEach(treasureChest -> {
            if (isLocationInWorld(treasureChest.location, worldUUID)) removedChests.add(treasureChest);
        });
        removedChests.forEach(treasureChest -> treasureChest.deactivateInstance(worldUUID));
    }

    public static void shutdown() {
        Set<TreasureChest> knownChests = Collections.newSetFromMap(new IdentityHashMap<>());
        knownChests.addAll(treasureChestHashMap.values());
        knownChests.addAll(instancedTreasureChests.values());
        knownChests.forEach(treasureChest -> {
            treasureChest.cancelRestock();
            treasureChest.unregisterPersistentHandler();
        });
        treasureChestHashMap.clear();
        instancedTreasureChests.clear();
    }

    public static TreasureChest getTreasureChest(Location location) {
        return getTreasureChestHashMap().get(location);
    }

    private static boolean isLocationInWorld(Location location, UUID worldUUID) {
        return location != null && location.getWorld() != null && location.getWorld().getUID().equals(worldUUID);
    }

    private void registerPersistentHandler() {
        unregisterPersistentHandler();
        persistentObjectHandler = new PersistentObjectHandler(this);
    }

    private void unregisterPersistentHandler() {
        if (persistentObjectHandler == null) return;
        persistentObjectHandler.remove();
        persistentObjectHandler = null;
    }

    private boolean registerChest() {
        if (!hasValidConfiguration() || !hasLoadedWorld()) return false;
        treasureChestHashMap.put(location, this);
        return true;
    }

    private boolean isRegistered() {
        return location != null && treasureChestHashMap.get(location) == this;
    }

    private boolean hasValidConfiguration() {
        return customTreasureChestConfigFields.isEnabled() &&
                customTreasureChestConfigFields.getChestMaterial() != null;
    }

    private boolean hasLoadedWorld() {
        if (location == null || location.getWorld() == null) return false;
        return Bukkit.getWorld(location.getWorld().getUID()) != null;
    }

    private boolean canGenerateChest() {
        if (!isRegistered() || !hasValidConfiguration() || !hasLoadedWorld()) return false;
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    private void cancelRestock() {
        if (restockTask != null && !restockTask.isCancelled()) restockTask.cancel();
        restockTask = null;
    }

    private void deactivateInstance(UUID worldUUID) {
        cancelRestock();
        unregisterPersistentHandler();
        if (!isLocationInWorld(location, worldUUID)) return;
        treasureChestHashMap.remove(location, this);
        location = null;
    }

    private void scheduleRestock() {
        cancelRestock();
        if (!isRegistered() || !hasValidConfiguration() || !hasLoadedWorld()) return;

        long secondsUntilRestock = Math.max(0L, restockTime - Instant.now().getEpochSecond());
        long delayTicks = secondsUntilRestock > Long.MAX_VALUE / 20L
                ? Long.MAX_VALUE
                : secondsUntilRestock * 20L;
        restockTask = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
            restockTask = null;
            if (canGenerateChest()) generateChest();
        }, delayTicks);
    }

    private void generateChest() {
        if (!canGenerateChest()) return;
        try {
            if (!location.getBlock().getType().equals(customTreasureChestConfigFields.getChestMaterial()))
                location.getBlock().setType(customTreasureChestConfigFields.getChestMaterial());
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

        if (!customTreasureChestConfigFields.isInstanced()) scheduleRestock();

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
            return Round.twoDecimalPlaces(seconds / 60D / 60 / 24) + "days";
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
        cancelRestock();
        unregisterPersistentHandler();
        CustomTreasureChestsConfig.removeTreasureChestEntry(location, customTreasureChestConfigFields.getFilename());
        if (location != null && location.getWorld() != null)
            location.getBlock().setBlockData(Material.AIR.createBlockData());
        treasureChestHashMap.remove(location, this);
    }

    @Override
    public void chunkLoad() {
        if (!registerChest()) return;
        scheduleRestock();
    }

    @Override
    public void chunkUnload() {
        cancelRestock();
    }

    @Override
    public void worldLoad(World world) {
        this.location = ConfigurationLocation.serialize(locationString);
        if (!registerChest()) return;
        scheduleRestock();
    }

    @Override
    public void worldUnload() {
        cancelRestock();
        treasureChestHashMap.remove(location, this);
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
