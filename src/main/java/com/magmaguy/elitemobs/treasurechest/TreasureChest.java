package com.magmaguy.elitemobs.treasurechest;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestsConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WeightedProbability;
import com.magmaguy.magmacore.util.ChatColorConverter;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
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

        if (ThreadLocalRandom.current().nextDouble() < customTreasureChestConfigFields.getMimicChance()) doMimic();
        else doTreasure(player);

        player.playSound(player.getLocation(), SoundsConfig.treasureChestOpenSound, 1, 1);

        if (customTreasureChestConfigFields.getDropStyle().equals(DropStyle.GROUP)) {
            if (customTreasureChestConfigFields.isInstanced()) {
                blacklistedPlayersInstance.add(player.getUniqueId());
            } else if (customTreasureChestConfigFields.getRestockTimers() != null) {
                customTreasureChestConfigFields.getRestockTimers().add(cooldownStringConstructor(player));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        customTreasureChestConfigFields.getRestockTimers().removeIf(restockTime -> restockTime.split(":")[0].equals(player.getUniqueId().toString()));
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 20L * 60 * customTreasureChestConfigFields.getRestockTimer());
            }
            return;
        }

        location.getBlock().setType(Material.AIR);

        restockTime = cooldownTime();
        customTreasureChestConfigFields.setRestockTime(location, restockTime);

        if (!customTreasureChestConfigFields.isInstanced())
            Bukkit.getScheduler().scheduleSyncDelayedTask(MetadataHandler.PLUGIN, this::generateChest, 20L * 60 * customTreasureChestConfigFields.getRestockTimer());

    }

    private void doMimic() {
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
        customBossEntity.spawn(location, randomizeTier(), false);
    }

    private void doTreasure(Player player) {
        this.customTreasureChestConfigFields.getCustomLootTable().treasureChestDrop(player, customTreasureChestConfigFields.getChestTier(), location);
    }

    private int randomizeTier() {
        return customTreasureChestConfigFields.getChestTier() * 10 + ThreadLocalRandom.current().nextInt(11);
    }

    private void lowRankMessage(Player player) {
        player.sendMessage(ChatColorConverter.convert(DefaultConfig.getChestLowRankMessage().replace("$rank", GuildRank.getRankName(Math.max(0, customTreasureChestConfigFields.getChestTier() - 10), customTreasureChestConfigFields.getChestTier()))));
    }

    private void groupTimerCooldownMessage(Player player, long targetTime) {
        player.sendMessage(ChatColorConverter.convert(DefaultConfig.getChestCooldownMessage().replace("$time", timeConverter(targetTime - Instant.now().getEpochSecond()))));
    }

    private boolean playerIsInCooldown(Player player) {
        if (customTreasureChestConfigFields.isInstanced())
            return blacklistedPlayersInstance.contains(player.getUniqueId());
        if (customTreasureChestConfigFields.getRestockTimers() == null) return false;
        for (String string : customTreasureChestConfigFields.getRestockTimers())
            if (string.split(":")[0].equals(player.getUniqueId().toString()))
                return true;
        return false;
    }

    private long getPlayerCooldown(Player player) {
        for (String string : customTreasureChestConfigFields.getRestockTimers())
            if (string.split(":")[0].equals(player.getUniqueId().toString()))
                return Long.parseLong(string.split(":")[1]);
        return 0;
    }

    private String cooldownStringConstructor(Player player) {
        return player.getUniqueId() + ":" + cooldownTime();
    }

    private long cooldownTime() {
        return Instant.now().getEpochSecond() + 60L * this.customTreasureChestConfigFields.getRestockTimer();
    }

    private String timeConverter(long seconds) {
        if (seconds < 60 * 2)
            return seconds + " seconds";
        if (seconds < 60 * 60 * 2)
            return Round.twoDecimalPlaces(seconds / 60D) + "minutes";
        if (seconds < 60 * 60 * 48)
            return Round.twoDecimalPlaces(seconds / 60D / 60) + "hours";
        else
            return Round.twoDecimalPlaces(seconds / 60D / 60 / 48) + "days";
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
            if (GuildRank.getMaxGuildRank(event.getPlayer()) < treasureChest.customTreasureChestConfigFields.getChestTier())
                treasureChest.lowRankMessage(event.getPlayer());
            else
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
