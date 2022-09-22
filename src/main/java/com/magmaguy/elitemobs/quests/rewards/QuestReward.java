package com.magmaguy.elitemobs.quests.rewards;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.UUID;

public class QuestReward implements Serializable {

    @Getter
    private final int rewardLevel;
    @Getter
    private final UUID playerUUID;
    @Getter
    private CustomLootTable customLootTable = null;

    public QuestReward(int questLevel, QuestObjectives questObjectives, Player player) {
        this.rewardLevel = questLevel * 10;
        this.playerUUID = player.getUniqueId();
        int killAmount = questObjectives.getObjectives().stream().mapToInt(Objective::getTargetAmount).sum();
        int baselineReward = (int) (rewardLevel / 2D * killAmount * GuildRank.currencyBonusMultiplier(playerUUID));
        customLootTable = new CustomLootTable();
        customLootTable.generateCurrencyEntry(baselineReward);
        ItemStack itemReward = LootTables.generateItemStack(Math.min(rewardLevel, PlayerData.getMaxGuildLevel(player.getUniqueId()) * 10), player, null);
        customLootTable.generateEliteEntry(itemReward);
    }

    /**
     * This generates a list of custom quest rewards based on configuration file entries. It uses the following formats:
     * Custom Items: filename=X.yml:amount=Y:chance=Z
     * Vanilla Items: material=X:amount=Y:chance=Z
     */
    public QuestReward(CustomQuestsConfigFields customQuestsConfigFields, Player player) {
        this.rewardLevel = customQuestsConfigFields.getQuestLevel() * 10;
        this.playerUUID = player.getUniqueId();
        this.customLootTable = new CustomLootTable(customQuestsConfigFields);
    }

    public void doRewards() {
        customLootTable.questDrop(Bukkit.getPlayer(playerUUID), rewardLevel);
    }

}
