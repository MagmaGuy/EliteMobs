package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.config.menus.premade.QuestMenuConfig;
import com.magmaguy.elitemobs.utils.DeepCopy;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class EliteQuest implements Serializable {

    public static EliteQuest generateRandomQuest(int questLevel) {
        return new EliteQuest(questLevel);
    }

    private static EntityType generateRandomEntityType() {
        Object[] array = EliteMobProperties.getValidMobTypes().toArray();
        return (EntityType) array[ThreadLocalRandom.current().nextInt(array.length)];
    }

    private static final HashMap<Player, EliteQuest> questTracker = new HashMap();

    private static HashMap<Player, EliteQuest> getQuestTracker() {
        return questTracker;
    }

    public static void addPlayerInQuests(Player player, EliteQuest eliteQuest) {
        try {
            getQuestTracker().put(player, (EliteQuest) DeepCopy.copyObject(eliteQuest));
        } catch (Exception ex) {
            new WarningMessage("Failed to clone quest objective! Report this to the dev.");
        }
    }

    public static boolean hasPlayerQuest(Player player) {
        return getQuestTracker().containsKey(player);
    }

    public static EliteQuest getPlayerQuest(Player player) {
        if (!questTracker.containsKey(player)) return null;
        return questTracker.get(player);
    }

    public static void removePlayersInQuests(Player player) {
        getQuestTracker().remove(player);
    }

    public static void cancelPlayerQuest(Player player) {
        player.sendMessage(QuestMenuConfig.questCancelMessage);
        removePlayersInQuests(player);
    }

    /*
    Actual class constructor
     */
    private final QuestObjective questObjective;
    private final UUID uuid = UUID.randomUUID();

    public EliteQuest(int questLevel) {
        this.questObjective = new QuestObjective(
                ThreadLocalRandom.current().nextInt(8) * questLevel + 8
                , questLevel * 10,
                generateRandomEntityType(),
                questLevel);
    }

    public UUID getUuid() {
        return uuid;
    }

    public QuestObjective getQuestObjective() {
        return questObjective;
    }

    public boolean processQuestProgression(EliteMobEntity eliteMobEntity, Player player) {
        return getQuestObjective().processQuestProgression(eliteMobEntity, player);
    }

    public String getQuestStatus() {
        return QuestMenuConfig.questStatusMessage
                .replace("$currentAmount", getQuestObjective().getCurrentKills() + "")
                .replace("$objectiveAmount", getQuestObjective().getObjectiveKills() + "")
                .replace("$objectiveName", getQuestObjective().getEliteMobName());
    }

    public ItemStack generateQuestItemStack(Player player) {
        HashMap<String, String> placeholderReplacementPairs = new HashMap<>();
        String objectiveName = EliteMobProperties.getPluginData(getQuestObjective().getEntityType()).getName()
                .replace("$level", MobTierCalculator.findMobLevel(getQuestObjective().getMinimumEliteMobTier()) + "+");
        placeholderReplacementPairs.put("$objectiveAmount", getQuestObjective().getObjectiveKills() + "");
        placeholderReplacementPairs.put("$currentAmount", getQuestObjective().getCurrentKills() + "");
        placeholderReplacementPairs.put("$objectiveName", objectiveName);
        placeholderReplacementPairs.put("$rewardAmount", questObjective.getQuestReward().getRewardMessage(player));

        return ItemStackSerializer.itemStackPlaceholderReplacer(QuestMenuConfig.killObjectiveButton, placeholderReplacementPairs);

    }

}
