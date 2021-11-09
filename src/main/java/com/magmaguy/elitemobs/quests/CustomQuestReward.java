package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CustomQuestReward implements Serializable {

    @Getter
    private List<RewardEntry> rewardEntries = new ArrayList<>();

    /**
     * This generates a list of custom quest rewards based on configuration file entries. It uses the following formats:
     * Custom Items: filename=X.yml:amount=Y:chance=Z
     * Vanilla Items: material=X:amount=Y:chance=Z
     * Commands: command=$player XYZ
     *
     */
    public CustomQuestReward(CustomQuestsConfigFields customQuestsConfigFields) {
        for (String string : customQuestsConfigFields.getCustomRewardsList()) {
            String[] splitByColon = string.split(":");
            String filename = null;
            int amount = 1;
            double chance = 1d;
            Material material = null;
            String command = null;
            for (String subString : splitByColon) {
                String[] splitByEquals = subString.split("=");
                switch (splitByEquals[0]) {
                    case "filename":
                        filename = splitByEquals[1];
                        if (CustomItem.getCustomItem(filename) == null) {
                            new WarningMessage("Could not detect custom item " + filename + " in customitems folder for reward in quest " + customQuestsConfigFields.getFilename() + ". Is it correct?");
                            filename = null;
                        }
                        break;
                    case "amount":
                        try {
                            amount = Integer.parseInt(splitByEquals[1]);
                        } catch (Exception ex) {
                            new WarningMessage("Invalid amount in entry " + splitByEquals[1] + " for quest " + customQuestsConfigFields + " ! Defaulting to 1...");
                        }
                        break;
                    case "chance":
                        try {
                            chance = Double.parseDouble(splitByEquals[1]);
                        } catch (Exception ex) {
                            new WarningMessage("Invalid chance in entry " + splitByEquals[1] + " for quest " + customQuestsConfigFields + " ! Defaulting to 1...");
                        }
                        break;
                    case "material":
                        try {
                            material = Material.valueOf(splitByEquals[1]);
                        } catch (Exception ex) {
                            new WarningMessage("Invalid material in entry " + splitByEquals[1] + " for quest " + customQuestsConfigFields + " ! Skipping...");
                        }
                    case "command":
                        command = splitByEquals[1];
                        break;
                    default:
                        new WarningMessage("Invalid entry " + subString + " for quest " + customQuestsConfigFields.getFile() + " ! Skipping this entry...");
                }
            }

            if (filename != null) {
                new RewardEntry(CustomItem.getCustomItem(filename), chance, amount);
            } else if (material != null) {
                new RewardEntry(new ItemStack(material), chance, amount);
            } else if (command != null) {
                new RewardEntry(command, chance, amount);
            } else {
                new WarningMessage("Failed to correctly register reward " + string + " for quest " + customQuestsConfigFields.getFilename());
            }
        }
    }

    public void doRewards(UUID playerUUID, int questLevel) {
        rewardEntries.forEach((rewardEntries) -> rewardEntries.doReward(playerUUID, questLevel));
    }

    public class RewardEntry {
        public CustomItem customItem;
        public ItemStack itemStack;
        public String command;
        public double chance;
        public int amount;

        public RewardEntry(CustomItem customItem, double chance, int amount) {
            this.customItem = customItem;
            this.chance = chance;
            this.amount = amount;
            rewardEntries.add(this);
        }

        public RewardEntry(ItemStack itemStack, double chance, int amount) {
            this.itemStack = itemStack;
            this.chance = chance;
            this.amount = amount;
            rewardEntries.add(this);
        }

        public RewardEntry(String command, double chance, int amount) {
            this.command = command;
            this.chance = chance;
            this.amount = amount;
            rewardEntries.add(this);
        }

        public void doReward(UUID playerUUID, int questLevel) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (ThreadLocalRandom.current().nextDouble() < chance)
                for (int i = 0; i < amount; i++)
                    if (customItem != null)
                        CustomItem.dropPlayerLoot(player, questLevel * 10, customItem.getFileName(), player.getLocation());
                    else if (itemStack != null)
                        player.getInventory().addItem(itemStack);
                    else if (command != null)
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("$player", player.getName()));
                    else
                        new WarningMessage("Quest failed to dispatch reward! Report this to the dev!", true);
        }
    }
}
