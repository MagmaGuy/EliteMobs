package com.magmaguy.elitemobs.items.itemconstructor;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.AdventurersGuildConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class SpecialLoot {

    private SpecialLootType specialLootType = null;
    private int minAmount = 0, maxAmount;
    @Getter
    @Setter
    private int minLevel = 0, maxLevel;
    private boolean ignorePlayerLevel = false;
    @Getter
    private double chance = 1;
    @Getter
    private boolean dynamicLevel = false;

    //format: scrap:level=x-y:amount=x-y:ignorePlayerLevel=x:chance=1
    public SpecialLoot(String rawConfigurationString) {
        String[] configSubstrings = rawConfigurationString.split(":");
        try {
            specialLootType = SpecialLootType.valueOf(configSubstrings[0].toUpperCase());
        } catch (Exception ex) {
            new WarningMessage("Invalid entry for " + rawConfigurationString);
        }

        for (String option : configSubstrings)
            try{
            if (option.contains("level")) {
                String finalOption = option.split("=")[1];
                if (finalOption.contains("-")) {
                    String[] minMax = finalOption.split("-");
                    minLevel = Integer.parseInt(minMax[0]);
                    maxLevel = Integer.parseInt(minMax[1]);
                } else if (finalOption.equalsIgnoreCase("dynamic")) {
                    //this get set elsewhere when it is dynamic
                    minLevel = maxLevel = 1;
                    dynamicLevel = true;
                } else {
                    minLevel = Integer.parseInt(finalOption);
                    maxLevel = Integer.parseInt(finalOption);
                }
            } else if (option.contains("amount")) {
                String finalOption = option.split("=")[1];
                if (finalOption.contains("-")) {
                    String[] minMax = finalOption.split("-");
                    minAmount = Integer.parseInt(minMax[0]);
                    maxAmount = Integer.parseInt(minMax[1]);
                } else {
                    minAmount = Integer.parseInt(finalOption);
                    maxAmount = Integer.parseInt(finalOption);
                }
            } else if (option.contains("ignorePlayerLevel")) {
                String finalOption = option.split("=")[1];
                ignorePlayerLevel = Boolean.parseBoolean(finalOption);
            } else if (option.contains("chance")) {
                String finalOption = option.split("=")[1];
                chance = Double.parseDouble(finalOption);
            }} catch (Exception exception){
                new WarningMessage("Failed to load Special Item! Problematic entry: " + option);
            }
    }

    public static boolean isSpecialLootEntry(String rawConfigEntry) {
        if (rawConfigEntry.isEmpty()) return false;
        String configSection = rawConfigEntry.split(":")[0];
        if (configSection.isEmpty()) return false;
        return configSection.equalsIgnoreCase("SCRAP") || configSection.equalsIgnoreCase("UPGRADE_ITEM");
    }

    public ItemStack generateItemStack(Player player, int minLevel, int maxLevel) {
        if (specialLootType == null) return null;

        int amount;
        if (minAmount == maxAmount)
            amount = minAmount;
        else
            amount = Math.min(ThreadLocalRandom.current().nextInt(minAmount, maxAmount), 64);

        int level;
        if (!dynamicLevel) {
            if (this.minLevel == this.maxLevel)
                level = this.minLevel;
            else
                level = ThreadLocalRandom.current().nextInt(this.minLevel, this.maxLevel);
        } else {
            if (minLevel == maxLevel)
                level = minLevel;
            else
                level = ThreadLocalRandom.current().nextInt(minLevel, maxLevel);
        }
        if (!ignorePlayerLevel)
            if (AdventurersGuildConfig.isGuildLootLimiter())
                level = Math.min(level, GuildRank.getMaxGuildRank(player) * 10);
        ItemStack itemStack;
        switch (specialLootType) {
            case SCRAP:
                itemStack = ItemConstructor.constructScrapItem(level, player, false);
                break;
            case UPGRADE_ITEM:
            default:
                itemStack = ItemConstructor.constructUpgradeItem(level, player, false);
                break;
        }

        itemStack.setAmount(amount);
        return itemStack;
    }

    public enum SpecialLootType {
        SCRAP,
        UPGRADE_ITEM
    }

}
