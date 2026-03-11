package com.magmaguy.elitemobs.quests.rewards;

import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DynamicDungeonInstance;
import com.magmaguy.elitemobs.items.LootTables;
import com.magmaguy.elitemobs.items.customloottable.CustomLootTable;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.DynamicQuestLevel;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class QuestReward implements Serializable {

    @Getter
    private final int rewardLevel;
    @Getter
    private final UUID playerUUID;
    @Getter
    private CustomLootTable customLootTable = null;
    private boolean generateProceduralReward = false;

    public QuestReward(int rewardLevel, QuestObjectives questObjectives, Player player) {
        this.rewardLevel = DynamicQuestLevel.clamp(rewardLevel);
        this.playerUUID = player.getUniqueId();
        int killAmount = questObjectives.getObjectives().stream().mapToInt(Objective::getTargetAmount).sum();
        int baselineReward = (int) (this.rewardLevel / 2D * killAmount);
        customLootTable = new CustomLootTable();
        customLootTable.generateCurrencyEntry(baselineReward);
        // Item reward is generated at reward time using the player's current level, not the quest level
        this.generateProceduralReward = true;
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

    /**
     * Gets the effective reward level. Uses the dynamic dungeon's selected level if the player is
     * in one, otherwise falls back to the player's combat level. Has a small chance of +1 bonus.
     */
    private int getEffectiveRewardLevel() {
        Player player = Bukkit.getPlayer(playerUUID);
        int level;
        // If in a dynamic dungeon, use the dungeon's selected level
        if (player != null) {
            MatchInstance matchInstance = PlayerData.getMatchInstance(player);
            if (matchInstance instanceof DynamicDungeonInstance dynamicDungeonInstance) {
                level = dynamicDungeonInstance.getSelectedLevel();
                if (ThreadLocalRandom.current().nextDouble() < 0.1) level++;
                return generateProceduralReward ? DynamicQuestLevel.clamp(level) : Math.max(1, level);
            }
        }
        // Fallback: use the player's combat level
        level = CombatLevelCalculator.calculateCombatLevel(playerUUID);
        if (ThreadLocalRandom.current().nextDouble() < 0.1) level++;
        return generateProceduralReward ? DynamicQuestLevel.clamp(level) : Math.max(1, level);
    }

    public void doRewards() {
        Player player = Bukkit.getPlayer(playerUUID);
        int effectiveLevel = getEffectiveRewardLevel();
        // Drop custom loot table entries (currency, custom items) at the player's level
        customLootTable.questDrop(player, effectiveLevel);
        // For dynamic quests, generate a procedural item reward at the player's level
        if (generateProceduralReward) {
            ItemStack itemReward = LootTables.generateItemStack(effectiveLevel, player, null);
            if (itemReward != null) {
                HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(itemReward);
                leftOvers.values().forEach(leftOver -> player.getWorld().dropItem(player.getLocation(), leftOver));
            }
        }
    }

    public List<ItemStack> previewRewards() {
        Player player = Bukkit.getPlayer(playerUUID);
        int effectiveLevel = getEffectiveRewardLevel();
        List<ItemStack> items = customLootTable.previewQuestDrop(player, effectiveLevel);
        if (generateProceduralReward) {
            ItemStack itemReward = LootTables.generateItemStack(effectiveLevel, player, null);
            if (itemReward != null) items.add(itemReward);
        }
        return items;
    }

}
