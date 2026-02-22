package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CustomLootTable implements Serializable {

    @Getter
    private final List<CustomLootEntry> entries = new ArrayList<>();
    private final Map<Integer, List<CustomLootEntry>> waveRewards = new HashMap();

    public CustomLootTable() {
    }

    public CustomLootTable(CustomBossesConfigFields customBossesConfigFields) {
        List<Object> rawStrings = customBossesConfigFields.getUniqueLootList();
        parseConfig(rawStrings, customBossesConfigFields.getFilename());
    }

    public CustomLootTable(CustomTreasureChestConfigFields treasureChestConfigFields) {
        List<Object> rawStrings = treasureChestConfigFields.getLootList();
        parseConfig(rawStrings, treasureChestConfigFields.getFilename());
    }

    public CustomLootTable(CustomQuestsConfigFields questsConfigFields) {
        List<String> rawStrings = questsConfigFields.getCustomRewardsList();
        parseConfig(rawStrings, questsConfigFields.getFilename());
    }

    public CustomLootTable(CustomArenasConfigFields customArenasConfigFields) {
        List<Object> rawStrings = customArenasConfigFields.getRawArenaRewards();
        parseConfig(rawStrings, customArenasConfigFields.getFilename());
    }

    private void parseConfig(List<?> lootTable, String filename) {
        if (lootTable == null) return;
        for (Object object : lootTable)
            if (object instanceof String rawString) {
                switch (rawString.split(":")[0].toLowerCase(Locale.ROOT)) {
                    case "minecraft":
                        new VanillaCustomLootEntry(entries, rawString, filename);
                        break;
                    default:
                        if (rawString.toLowerCase(Locale.ROOT).contains("currencyamount="))
                            new CurrencyCustomLootEntry(entries, rawString, filename);
                        else if (rawString.contains("material="))
                            new VanillaCustomLootEntry(entries, rawString, filename);
                        else if (rawString.contains("command=")) new CommandLootTable(entries, rawString, filename);
                        else new EliteCustomLootEntry(entries, rawString, filename);
                }
            }
            else if (object instanceof Map<?, ?> configMap) {
                //This is used for the instanced loot
                if (((Map<?, ?>) object).containsKey("currencyAmount") ||
                        ((Map<?, ?>) object).containsKey("currencyamount")) {
                    new CurrencyCustomLootEntry(entries, configMap, filename);
                } else if (((Map<?, ?>) object).containsKey("material")) {
                    new VanillaCustomLootEntry(entries, (Map<String, Object>) configMap, filename);
                } else
                    new EliteCustomLootEntry(entries, configMap, filename);
            }
        for (CustomLootEntry customLootEntry : entries) {
            if (customLootEntry.getWave() > 0) {
                if (this.waveRewards.get(customLootEntry.getWave()) != null) {
                    List<CustomLootEntry> rewards = this.waveRewards.get(customLootEntry.getWave());
                    rewards.add(customLootEntry);
                    this.waveRewards.put(customLootEntry.getWave(), rewards);
                } else waveRewards.put(customLootEntry.getWave(), new ArrayList<>(List.of(customLootEntry)));
            }
        }
    }

    public void generateCurrencyEntry(int currencyAmount) {
        new CurrencyCustomLootEntry(entries, currencyAmount);
    }

    public void generateEliteEntry(ItemStack itemStack) {
        new ItemStackCustomLootEntry(entries, itemStack);
    }

    public void bossDrop(Player player, int level, Location dropLocation, EliteEntity eliteEntity) {
        for (CustomLootEntry customLootEntry : entries) {
            if (customLootEntry.willDrop(player)) {
                if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory())
                    customLootEntry.directDrop(level, player, eliteEntity);
                else {
                    customLootEntry.locationDrop(level, player, dropLocation, eliteEntity);
                }
            }
        }
    }

    public void treasureChestDrop(Player player, int chestLevel, Location dropLocation) {
        boolean anythingDropped = false;
        for (CustomLootEntry customLootEntry : entries)
            if (customLootEntry.willDrop(player)) {
                anythingDropped = true;
                if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
                    customLootEntry.directDrop(chestLevel * 10, player);
                } else {
                    customLootEntry.locationDrop(chestLevel * 10, player, dropLocation);
                }
            }
        if (!anythingDropped)
            player.sendMessage(DefaultConfig.getTreasureChestNoDropMessage());
    }

    public void treasureChestDropAtLevel(Player player, int level, Location dropLocation) {
        boolean anythingDropped = false;
        for (CustomLootEntry customLootEntry : entries)
            if (customLootEntry.willDrop(player)) {
                anythingDropped = true;
                if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory()) {
                    if (customLootEntry instanceof EliteCustomLootEntry eliteCustomLootEntry) {
                        int exactLevel = level;
                        if (eliteCustomLootEntry.isScalableItem())
                            exactLevel = Math.max(1, level + ThreadLocalRandom.current().nextInt(-1, 2));
                        eliteCustomLootEntry.directDropExactLevel(exactLevel, player);
                    }
                    else
                        customLootEntry.directDrop(level, player);
                } else {
                    if (customLootEntry instanceof EliteCustomLootEntry eliteCustomLootEntry) {
                        int exactLevel = level;
                        if (eliteCustomLootEntry.isScalableItem())
                            exactLevel = Math.max(1, level + ThreadLocalRandom.current().nextInt(-1, 2));
                        eliteCustomLootEntry.locationDropExactLevel(exactLevel, player, dropLocation);
                    }
                    else
                        customLootEntry.locationDrop(level, player, dropLocation);
                }
            }
        if (!anythingDropped)
            player.sendMessage(DefaultConfig.getTreasureChestNoDropMessage());
    }

    public void treasureChestDropScalableToPlayerLevel(Player player, int chestLevel, int playerLevel, Location dropLocation) {
        boolean anythingDropped = false;
        int defaultLevel = chestLevel * 10;
        for (CustomLootEntry customLootEntry : entries)
            if (customLootEntry.willDrop(player)) {
                anythingDropped = true;
                if (customLootEntry instanceof EliteCustomLootEntry eliteCustomLootEntry &&
                        eliteCustomLootEntry.isScalableItem()) {
                    int scalableLevel = Math.max(1, playerLevel + ThreadLocalRandom.current().nextInt(-1, 2));
                    if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory())
                        eliteCustomLootEntry.directDropExactLevel(scalableLevel, player);
                    else
                        eliteCustomLootEntry.locationDropExactLevel(scalableLevel, player, dropLocation);
                    continue;
                }
                if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory())
                    customLootEntry.directDrop(defaultLevel, player);
                else
                    customLootEntry.locationDrop(defaultLevel, player, dropLocation);
            }
        if (!anythingDropped)
            player.sendMessage(DefaultConfig.getTreasureChestNoDropMessage());
    }

    public void questDrop(Player player, int questRewardLevel) {
        for (CustomLootEntry customLootEntry : entries)
            if (customLootEntry.willDrop(player)) customLootEntry.directDrop(questRewardLevel, player);
    }

    public List<ItemStack> previewQuestDrop(Player player, int questRewardLevel) {
        List<ItemStack> items = new ArrayList<>();
        for (CustomLootEntry customLootEntry : entries)
            if (customLootEntry.willDrop(player)) items.add(customLootEntry.previewDrop(questRewardLevel, player));
        return items;
    }

    public void arenaReward(Player player, int wave) {
        if (waveRewards.get(wave) == null) return;
        waveRewards.get(wave).forEach(reward -> {
            if (reward.willDrop(player)) reward.directDrop(reward.getItemLevel(), player);
        });
    }
}
