package com.magmaguy.elitemobs.quests.rewards;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestReward implements Serializable {

    @Getter
    private final List<RewardEntry> rewardEntries = new ArrayList<>();

    public QuestReward(int questLevel, QuestObjectives questObjectives, Player player) {
        int mobLevel = questLevel * 10;
        int killAmount = questObjectives.getObjectives().stream().mapToInt(Objective::getTargetAmount).sum();
        double baselineReward = mobLevel / 2D * killAmount;
        rewardEntries.add(new RewardEntry(baselineReward, 1d, 1, player));
        ItemStack itemReward = LootTables.generateItemStack(questLevel * 10, player, null);
        rewardEntries.add(new RewardEntry(itemReward, 1d, 1));
    }

    /**
     * This generates a list of custom quest rewards based on configuration file entries. It uses the following formats:
     * Custom Items: filename=X.yml:amount=Y:chance=Z
     * Vanilla Items: material=X:amount=Y:chance=Z
     * Commands: command=$player XYZ
     */
    public QuestReward(CustomQuestsConfigFields customQuestsConfigFields, Player player) {
        for (String string : customQuestsConfigFields.getCustomRewardsList()) {
            String[] splitByColon = string.split(":");
            String filename = null;
            int amount = 1;
            double chance = 1d;
            Material material = null;
            String command = null;
            double currencyAmount = 0;
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
                        break;
                    case "currencyAmount":
                        try {
                            currencyAmount = Double.parseDouble(splitByEquals[1]);
                        } catch (Exception ex) {
                            new WarningMessage("Invalid currencyAmount in entry " + splitByEquals[1] + " for quest " + customQuestsConfigFields + " ! Skipping...");
                        }
                        break;
                    case "command":
                        command = splitByEquals[1];
                        break;
                    default:
                        new WarningMessage("Invalid entry " + subString + " for quest " + customQuestsConfigFields.getFile() + " ! Skipping this entry...");
                }
            }

            if (filename != null) {
                rewardEntries.add(new RewardEntry(CustomItem.getCustomItem(filename).generateItemStack(customQuestsConfigFields.getQuestLevel() * 10, player), chance, amount));
            } else if (material != null) {
                rewardEntries.add(new RewardEntry(new ItemStack(material), chance, amount));
            } else if (command != null) {
                rewardEntries.add(new RewardEntry(command, chance, amount));
            } else if (currencyAmount != 0) {
                rewardEntries.add(new RewardEntry(currencyAmount, chance, amount, player));
            } else {
                new WarningMessage("Failed to correctly register reward " + string + " for quest " + customQuestsConfigFields.getFilename());
            }
        }
    }

    public void serializeRewards() {
        for (RewardEntry rewardEntry : rewardEntries)
            rewardEntry.serializeReward();
    }

    public void doRewards(UUID playerUUID, int questLevel) {
        rewardEntries.forEach((rewardEntries) -> rewardEntries.doReward(playerUUID, questLevel));
    }

}
