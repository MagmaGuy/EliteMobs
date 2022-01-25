package com.magmaguy.elitemobs.items.customloottable;

import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CustomLootTable implements Serializable {

    @Getter
    private final List<CustomLootEntry> entries = new ArrayList<>();

    public CustomLootTable() {
    }

    public CustomLootTable(CustomBossesConfigFields customBossesConfigFields) {
        List<String> rawStrings = customBossesConfigFields.getUniqueLootList();
        for (String rawString : rawStrings)
            switch (rawString.split(":")[0].toLowerCase()) {
                case "minecraft":
                    new VanillaCustomLootEntry(entries, rawString, customBossesConfigFields.getFilename());
                    break;
                case "scrap":
                case "upgrade_item":
                    new SpecialCustomLootEntry(entries, rawString, customBossesConfigFields.getFilename());
                    break;
                default:
                    if (rawString.contains("currencyAmount="))
                        new CurrencyCustomLootEntry(entries, rawString, customBossesConfigFields.getFilename());
                    else if (rawString.contains("material="))
                        new VanillaCustomLootEntry(entries, rawString, customBossesConfigFields.getFilename());
                    else
                        new EliteCustomLootEntry(entries, rawString, customBossesConfigFields.getFilename());
            }
    }

    public CustomLootTable(CustomTreasureChestConfigFields treasureChestConfigFields) {
        List<String> rawStrings = treasureChestConfigFields.getLootList();
        for (String rawString : rawStrings)
            switch (rawString.split(":")[0].toLowerCase()) {
                case "minecraft":
                    new VanillaCustomLootEntry(entries, rawString, treasureChestConfigFields.getFilename());
                    break;
                case "scrap":
                case "upgrade_item":
                    new SpecialCustomLootEntry(entries, rawString, treasureChestConfigFields.getFilename());
                    break;
                default:
                    if (rawString.contains("currencyAmount="))
                        new CurrencyCustomLootEntry(entries, rawString, treasureChestConfigFields.getFilename());
                    else if (rawString.contains("material="))
                        new VanillaCustomLootEntry(entries, rawString, treasureChestConfigFields.getFilename());
                    else
                        new EliteCustomLootEntry(entries, rawString, treasureChestConfigFields.getFilename());
            }
    }

    public CustomLootTable(CustomQuestsConfigFields questsConfigFields) {
        List<String> rawStrings = questsConfigFields.getCustomRewardsList();
        for (String rawString : rawStrings)
            switch (rawString.split(":")[0].toLowerCase()) {
                case "minecraft":
                    new VanillaCustomLootEntry(entries, rawString, questsConfigFields.getFilename());
                    break;
                case "scrap":
                case "upgrade_item":
                    new SpecialCustomLootEntry(entries, rawString, questsConfigFields.getFilename());
                    break;
                default:
                    if (rawString.contains("currencyAmount="))
                        new CurrencyCustomLootEntry(entries, rawString, questsConfigFields.getFilename());
                    else if (rawString.contains("material="))
                        new VanillaCustomLootEntry(entries, rawString, questsConfigFields.getFilename());
                    else
                        new EliteCustomLootEntry(entries, rawString, questsConfigFields.getFilename());
            }
    }

    public void generateCurrencyEntry(int currencyAmount) {
        new CurrencyCustomLootEntry(entries, currencyAmount);
    }

    public void generateEliteEntry(ItemStack itemStack) {
        new ItemStackCustomLootEntry(entries, itemStack);
    }

    public void bossDrop(Player player, int level, Location dropLocation) {
        for (CustomLootEntry customLootEntry : entries)
            if (customLootEntry.willDrop(player))
                if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory())
                    customLootEntry.directDrop(level, player);
                else
                    customLootEntry.locationDrop(level, player, dropLocation);
    }

    public void treasureChestDrop(Player player, int chestLevel, Location dropLocation) {
        for (CustomLootEntry customLootEntry : entries)
            if (customLootEntry.willDrop(player)) {
                if (ItemSettingsConfig.isPutLootDirectlyIntoPlayerInventory())
                    customLootEntry.directDrop(chestLevel * 10, player);
                else
                    customLootEntry.locationDrop(chestLevel * 10, player, dropLocation);
            }
    }

    public void questDrop(Player player, int questRewardLevel) {
        for (CustomLootEntry customLootEntry : entries)
            if (customLootEntry.willDrop(player))
                customLootEntry.directDrop(questRewardLevel, player);
    }
}
