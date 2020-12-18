package com.magmaguy.elitemobs.treasurechest;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powerstances.VisualItemInitializer;
import com.magmaguy.elitemobs.utils.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TreasureChest {

    private static final HashMap<String, TreasureChest> treasureChestHashMap = new HashMap<>();

    public static HashMap<String, TreasureChest> getTreasureChestHashMap() {
        return treasureChestHashMap;
    }

    public static TreasureChest getTreasureChest(String key) {
        return getTreasureChestHashMap().get(key);
    }

    public static void initializeTreasureChest() {
        for (CustomTreasureChestConfigFields customTreasureChestConfigFields : CustomTreasureChestsConfig.getCustomTreasureChestConfigFields().values())
            new TreasureChest(customTreasureChestConfigFields);
    }

    private enum DropStyle {
        SINGLE,
        GROUP
    }

    private final CustomTreasureChestConfigFields customTreasureChestConfigFields;
    private final String fileName;
    private final String key;
    private final boolean isEnabled;
    private Material chestMaterial;
    private BlockFace facing;
    private int chestTier;
    private Location location;
    private DropStyle dropStyle;
    private int restockTimer;
    private List<String> lootList;
    private double mimicChance;
    private List<String> mimicCustomBossesList;
    private long restockTime;
    private List<String> restockTimes;
    private List<String> effects;

    public TreasureChest(CustomTreasureChestConfigFields customTreasureChestConfigFields) {

        this.customTreasureChestConfigFields = customTreasureChestConfigFields;
        this.fileName = customTreasureChestConfigFields.getFileName();
        this.key = ChatColorConverter.convert(fileName);
        this.isEnabled = customTreasureChestConfigFields.isEnabled();
        if (!isEnabled)
            return;
        try {
            this.chestMaterial = Material.getMaterial(customTreasureChestConfigFields.getChestMaterial());
        } catch (Exception ex) {
            new WarningMessage("Malformed material for " + this.fileName + " !");
            new WarningMessage("Material " + customTreasureChestConfigFields.getChestMaterial() + " is not a valid material!");
            return;
        }
        if (chestMaterial == null)
            return;

        if (this.chestMaterial.equals(Material.CHEST) || this.chestMaterial.equals(Material.TRAPPED_CHEST) ||
                this.chestMaterial.equals(Material.ENDER_CHEST) || this.chestMaterial.equals(Material.SHULKER_BOX))
            try {
                if (customTreasureChestConfigFields.getFacing() != null)
                    this.facing = BlockFace.valueOf(customTreasureChestConfigFields.getFacing().toUpperCase());
            } catch (Exception ex) {
                new WarningMessage("Malformed direction for " + this.fileName + " !");
                new WarningMessage("Valid directions: NORTH, SOUTH, EAST, WEST. Your input: " + customTreasureChestConfigFields.getFacing());
            }

        this.chestTier = customTreasureChestConfigFields.getChestTier();
        this.location = customTreasureChestConfigFields.getLocation();
        if (location == null)
            return;

        try {
            location.getChunk().load();
        } catch (Exception ex) {
            new WarningMessage("Failed to load location " + location.toString() + " - this location can not be loaded");
            new WarningMessage("Does the world " + location.getWorld() + " exist? Did the world name change or has the world been removed?");
            return;
        }

        if (!customTreasureChestConfigFields.getDropStyle().equalsIgnoreCase("single") &&
                !customTreasureChestConfigFields.getDropStyle().equalsIgnoreCase("group"))
            this.dropStyle = DropStyle.SINGLE;
        else
            this.dropStyle = DropStyle.valueOf(customTreasureChestConfigFields.getDropStyle().toUpperCase());
        this.restockTimer = customTreasureChestConfigFields.getRestockTimer();
        this.lootList = customTreasureChestConfigFields.getLootList();
        this.mimicChance = customTreasureChestConfigFields.getMimicChance();
        this.mimicCustomBossesList = customTreasureChestConfigFields.getMimicCustomBossesList();
        this.restockTime = customTreasureChestConfigFields.getRestockTime();
        this.restockTimes = customTreasureChestConfigFields.getRestockTimes();
        if (this.restockTimes == null) this.restockTimes = new ArrayList<>();
        this.effects = customTreasureChestConfigFields.getEfffects();

        generateChest();

        treasureChestHashMap.put(this.key, this);

    }

    private void generateChest() {
        try {
            if (!location.getWorld().getBlockAt(location).getType().equals(chestMaterial))
                location.getWorld().getBlockAt(location).setType(chestMaterial);
        } catch (Exception ex) {
            new WarningMessage("Custom Treasure Chest " + fileName + " has an invalid location and can not be placed.");
            return;
        }
        //todo: this doesn't support non- chest block types like the ender chest
        Chest chest = (Chest) location.getBlock().getState();
        chest.setCustomName(this.key);
        //todo: add block face
        chest.update();

        startEffects();
    }

    public void doInteraction(Player player) {

        if (dropStyle.equals(DropStyle.GROUP))
            if (playerIsInCooldown(player)) {
                groupTimerCooldownMessage(player, getPlayerCooldown(player));
                return;
            }

        if (ThreadLocalRandom.current().nextDouble() < mimicChance)
            doMimic();
        else
            doTreasure(player);

        if (dropStyle.equals(DropStyle.GROUP)) {
            restockTimes.add(cooldownStringConstructor(player));
            new BukkitRunnable() {
                @Override
                public void run() {
                    restockTimes.removeIf(restockTime -> restockTime.split(":")[0].equals(player.getUniqueId().toString()));
                }
            }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60 * this.restockTimer);
            return;
        }

        location.getBlock().setType(Material.AIR);

        restockTime = cooldownTime();
        customTreasureChestConfigFields.setRestockTime(restockTime);
        effectIsOn = false;
        new BukkitRunnable() {
            @Override
            public void run() {
                generateChest();
                effectIsOn = true;
                startEffects();
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60 * this.restockTimer);

    }

    private void doMimic() {
        HashMap<String, Double> weighedValues = new HashMap<>();
        for (String string : this.mimicCustomBossesList)
            try {
                String filename = string.split(":")[0];
                double weight = Double.valueOf(string.split(":")[1]);
                weighedValues.put(filename, weight);
            } catch (Exception ex) {
                new WarningMessage("Malformed custom boss entry for " + this.fileName + " !");
                new WarningMessage("Entry: " + string);
                new WarningMessage("Correct format: filename.yml:weight");
            }
        CustomBossEntity.constructCustomBoss(WeightedProbability.pickWeighedProbability(weighedValues), location, randomizeTier());
    }

    private void doTreasure(Player player) {
        for (String string : this.lootList)
            try {
                String filename = string.split(":")[0];
                double odds = Double.valueOf(string.split(":")[1]);
                if (ThreadLocalRandom.current().nextDouble() < odds)
                    CustomItem.dropPlayerLoot(player, randomizeTier(), filename, location);

            } catch (Exception ex) {
                if (string.equalsIgnoreCase("hyper_loot.yml"))

                    new WarningMessage("Malformed loot entry for " + this.fileName + " !");
                new WarningMessage("Entry: " + string);
                new WarningMessage("Correct format: filename.yml:odds");
            }
    }

    private int randomizeTier() {
        return chestTier * 10 + ThreadLocalRandom.current().nextInt(11);
    }

    private void lowRankMessage(Player player) {
        //todo: fix treasure chests to incorporate prestige ranks into them
        player.sendMessage(ChatColorConverter.convert("&7[EM] &cYour guild rank needs to be " + GuildRank.getRankName(0, chestTier)
                + " &cin order to open this chest!"));
    }

    private void groupTimerCooldownMessage(Player player, long targetTime) {
        player.sendMessage(ChatColorConverter.convert("&7[EM] &cYou've already opened this chest recently! Wait "
                + timeConverter(targetTime - Instant.now().getEpochSecond()) + "!"));
    }

    private boolean playerIsInCooldown(Player player) {
        for (String string : restockTimes)
            if (string.split(":")[0].equals(player.getUniqueId().toString()))
                return true;
        return false;
    }

    private long getPlayerCooldown(Player player) {
        for (String string : restockTimes)
            if (string.split(":")[0].equals(player.getUniqueId().toString()))
                return Long.parseLong(string.split(":")[1]);
        return 0;
    }

    private String cooldownStringConstructor(Player player) {
        return player.getUniqueId().toString() + ":" + cooldownTime();
    }

    private long cooldownTime() {
        return Instant.now().getEpochSecond() + 60 * this.restockTimer;
    }

    private String timeConverter(long seconds) {
        if (seconds < 60 * 2)
            return seconds + " seconds";
        if (seconds < 60 * 60 * 2)
            return Round.twoDecimalPlaces(seconds / 60) + "minutes";
        if (seconds < 60 * 60 * 48)
            return Round.twoDecimalPlaces(seconds / 60 / 60) + "hours";
        else
            return Round.twoDecimalPlaces(seconds / 60 / 60 / 48) + "days";
    }


    public void startEffects() {
        //todo: this is not good
        for (String string : this.effects) {
            try {
                Particle particle = Particle.valueOf(string);
                doParticleTrail(particle);
            } catch (Exception ex) {
            }
            try {
                Material material = Material.valueOf(string);
                doItemTrail(material);
            } catch (Exception ex) {
            }
        }
    }

    public boolean chunkIsLoaded = true;
    public boolean effectIsOn = true;

    private void doParticleTrail(Particle particle) {
        effectIsOn = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                //In case of chunk unload, stop the effect
                if (!chunkIsLoaded || !effectIsOn) {
                    cancel();
                    effectIsOn = false;
                    return;
                }
                //All conditions cleared, do the particle effect
                location.getWorld().spawnParticle(particle, location.clone().add(new Vector(0.5, 0.5, 0.5)), 1, 0.1, 0.1, 0.1, 0.05);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void doItemTrail(Material material) {
        effectIsOn = true;
        new BukkitRunnable() {

            @Override
            public void run() {
                //In case of chunk unload, stop the effect
                if (!chunkIsLoaded || !effectIsOn) {
                    cancel();
                    effectIsOn = false;
                    return;
                }

                //All conditions cleared, do the boss flair effect
                Item item = VisualItemInitializer.initializeItem(ItemStackGenerator.generateItemStack
                        (material, "visualItem", Arrays.asList(ThreadLocalRandom.current().nextDouble() + "")), location.clone().add(new Vector(0.5, 0.5, 0.5)));
                item.setVelocity(new Vector(
                        ThreadLocalRandom.current().nextDouble() / 5 - 0.10,
                        ThreadLocalRandom.current().nextDouble() / 5 - 0.10,
                        ThreadLocalRandom.current().nextDouble() / 5 - 0.10));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        item.remove();
                        EntityTracker.wipeEntity(item);
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 20);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }


    public static class TreasureChestEvents implements Listener {
        @EventHandler
        public void onPlayerInteract(InventoryOpenEvent event) {
            if (!getTreasureChestHashMap().containsKey(event.getView().getTitle())) return;
            event.setCancelled(true);
            TreasureChest treasureChest = getTreasureChest(event.getView().getTitle());
            if (GuildRank.getMaxGuildRank((Player) event.getPlayer()) < treasureChest.chestTier)
                treasureChest.lowRankMessage((Player) event.getPlayer());
            else
                treasureChest.doInteraction((Player) event.getPlayer());
        }

        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            for (TreasureChest treasureChest : getTreasureChestHashMap().values())
                if (ChunkLocationChecker.chunkLocationCheck(treasureChest.location, event.getChunk()))
                    treasureChest.effectIsOn = false;
        }

        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            for (TreasureChest treasureChest : getTreasureChestHashMap().values())
                if (ChunkLocationChecker.chunkLocationCheck(treasureChest.location, event.getChunk())) {
                    treasureChest.effectIsOn = true;
                    treasureChest.startEffects();
                }
        }

        @EventHandler
        public void onBreak(BlockBreakEvent event) {
            for (TreasureChest treasureChest : treasureChestHashMap.values())
                if (event.getBlock().getLocation().equals(treasureChest.location.getBlock().getLocation()))
                    event.setCancelled(true);
        }

    }

}
