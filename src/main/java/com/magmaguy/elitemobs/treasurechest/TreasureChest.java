package com.magmaguy.elitemobs.treasurechest;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
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
    private static final Map<String, Set<TreasureChest>> itemSources = new HashMap<>();
    private final Set<String> indexedItems = new HashSet<>();

    public static Collection<TreasureChest> chestsDropping(String itemFilename) {
        return List.copyOf(itemSources.getOrDefault(itemFilename, Set.of()));
    }

    private void removeItemSources() {
        for (String item : indexedItems) {
            Set<TreasureChest> sources = itemSources.get(item);
            if (sources == null) continue;
            sources.remove(this);
            if (sources.isEmpty()) itemSources.remove(item);
        }
        indexedItems.clear();
    }

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
        this.location = blockLocation(ConfigurationLocation.serialize(locationString));
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
        this.location = blockLocation(ConfigurationLocation.serializeWithInstance(instancedWorld, locationString));
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
        itemSources.clear();
    }

    public static void removeInstancedTreasureChests(World world) {
        if (world == null) return;
        UUID worldUUID = world.getUID();
        Set<TreasureChest> removedChests = Collections.newSetFromMap(new IdentityHashMap<>());
        treasureChestHashMap.entrySet().removeIf(entry -> {
            Location keyLocation = entry.getKey();
            Location chestLocation = entry.getValue().location;
            boolean remove = isLocationInWorld(keyLocation, worldUUID) || isLocationInWorld(chestLocation, worldUUID);
            if (remove) {
                entry.getValue().removeItemSources();
                removedChests.add(entry.getValue());
            }
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
        itemSources.clear();
        instancedTreasureChests.clear();
    }

    public static TreasureChest getTreasureChest(Location location) {
        return getTreasureChestHashMap().get(blockLocation(location));
    }

    private static Location blockLocation(Location location) {
        return location == null ? null : new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
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
        TreasureChest previous = treasureChestHashMap.put(location, this);
        if (previous != null) previous.removeItemSources();
        var table = customTreasureChestConfigFields.getCustomLootTable();
        if (table != null) for (var entry : table.getEntries()) {
            if (!(entry instanceof com.magmaguy.elitemobs.items.customloottable.EliteCustomLootEntry item)) continue;
            indexedItems.add(item.getFilename());
            itemSources.computeIfAbsent(item.getFilename(), ignored -> new HashSet<>()).add(this);
        }
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
        removeItemSources();
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

        long now = Instant.now().getEpochSecond();
        if (customTreasureChestConfigFields.getDropStyle().equals(DropStyle.GROUP)) {
            if (customTreasureChestConfigFields.isInstanced()) {
                if (!blacklistedPlayersInstance.add(player.getUniqueId())) return;
            } else {
                long expiry = customTreasureChestConfigFields.cooldownExpiry(player.getUniqueId());
                if (expiry > now) {
                    groupTimerCooldownMessage(player, expiry);
                    return;
                }
                if (restockTime > now) return;
                try {
                    customTreasureChestConfigFields.reserveCooldown(player.getUniqueId(), cooldownTime(), now);
                } catch (RuntimeException failure) {
                    Logger.warn("Could not reserve treasure chest " + customTreasureChestConfigFields.getFilename()
                            + ": " + failure.getMessage());
                    player.sendMessage("This chest could not save your claim. Please try again later.");
                    return;
                }
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
        String filename = WeightedProbability.pickWeighedProbability(weighedValues);
        CustomBossesConfigFields fields = CustomBossesConfig.getCustomBoss(filename);
        if (fields == null) {
            Logger.warn("Failed to spawn mimic for treasure chest " + customTreasureChestConfigFields.getFilename() + ": custom boss config was not found.");
            return;
        }

        DungeonInstance dungeonInstance = getDungeonInstance(player);
        CustomBossEntity customBossEntity = dungeonInstance == null
                ? CustomBossEntity.createCustomBossEntity(filename)
                : dungeonInstance.createEncounterBoss(fields, location);
        if (customBossEntity == null) return;

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
        DungeonInstance dungeonInstance = getDungeonInstance(player);
        return dungeonInstance instanceof DynamicDungeonInstance dynamic ? dynamic.getSelectedLevel() : null;
    }

    private DungeonInstance getDungeonInstance(Player player) {
        if (location == null || location.getWorld() == null) return null;
        if (player != null) {
            MatchInstance matchInstance = PlayerData.getMatchInstance(player);
            if (matchInstance instanceof DungeonInstance dungeonInstance
                    && location.getWorld().equals(dungeonInstance.getWorld())) return dungeonInstance;
        }
        // Fallback for edge cases where player data isn't available yet.
        for (DungeonInstance dungeonInstance : DungeonInstance.getDungeonInstances()) {
            if (location.getWorld().equals(dungeonInstance.getWorld())) return dungeonInstance;
        }
        return null;
    }

    private void groupTimerCooldownMessage(Player player, long targetTime) {
        player.sendMessage(DefaultConfig.getChestCooldownMessage().replace("$time", timeConverter(targetTime - Instant.now().getEpochSecond())));
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

    public boolean removeTreasureChest() {
        try {
            if (!CustomTreasureChestsConfig.removeTreasureChestEntry(location, customTreasureChestConfigFields.getFilename())) return false;
        } catch (RuntimeException failure) {
            Logger.warn("Could not save removal of chest " + customTreasureChestConfigFields.getFilename() + ": " + failure.getMessage());
            return false;
        }
        cancelRestock();
        unregisterPersistentHandler();
        if (location != null && location.getWorld() != null)
            location.getBlock().setBlockData(Material.AIR.createBlockData());
        treasureChestHashMap.remove(location, this);
        removeItemSources();
        return true;
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
        this.location = blockLocation(ConfigurationLocation.serializeWithInstance(world, locationString));
        if (!registerChest()) return;
        scheduleRestock();
    }

    @Override
    public void worldUnload() {
        cancelRestock();
        treasureChestHashMap.remove(location, this);
        removeItemSources();
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
            if (getTreasureChest(event.getBlock().getLocation()) != null) event.setCancelled(true);
        }
    }

}
