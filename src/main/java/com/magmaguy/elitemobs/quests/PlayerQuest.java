package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.config.menus.premade.QuestMenuConfig;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.quests.dynamic.KillAmountQuest;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class PlayerQuest implements Cloneable {

    @Override
    public PlayerQuest clone() throws CloneNotSupportedException {
        return (PlayerQuest) super.clone();
    }

    private static HashMap<Player, PlayerQuest> playersInQuests = new HashMap();

    public static HashMap<Player, PlayerQuest> getPlayersInQuests() {
        return playersInQuests;
    }

    public static void addPlayerInQuests(Player player, PlayerQuest playerQuest) {
        getPlayersInQuests().put(player, playerQuest);
    }

    public static boolean hasPlayerQuest(Player player) {
        return getPlayersInQuests().containsKey(player);
    }

    public static PlayerQuest getPlayerQuest(Player player) {
        if (!playersInQuests.containsKey(player)) return null;
        return playersInQuests.get(player);
    }

    public static void removePlayersInQuests(Player player) {
        getPlayersInQuests().remove(player);
    }

    public static void cancelPlayerQuest(Player player) {
        player.sendMessage(QuestMenuConfig.questCancelMessage);
        removePlayersInQuests(player);
    }

    public enum QuestType {
        DYNAMIC_KILL_AMOUNT_QUEST
    }

    private QuestType questType;
    private QuestObjective questObjective;
    private QuestReward questReward;

    public PlayerQuest(QuestType questType, QuestObjective questObjective, QuestReward questReward) {
        setQuestType(questType);
        setQuestObjective(questObjective);
        setQuestReward(questReward);
    }

    public QuestType getQuestType() {
        return questType;
    }

    public ItemStack generateQuestItemStack() {
        HashMap<String, String> placeholderReplacementPairs = new HashMap<>();
        String objectiveName = EliteMobProperties.getPluginData(getQuestObjective().getEntityType()).getName()
                .replace("$level", MobTierCalculator.findMobLevel(getQuestObjective().getMinimumEliteMobTier()) + "");
        placeholderReplacementPairs.put("$objectiveAmount", getQuestObjective().getObjectiveKills() + "");
        placeholderReplacementPairs.put("$currentAmount", getQuestObjective().getCurrentKills() + "");
        placeholderReplacementPairs.put("$objectiveName", objectiveName);
        placeholderReplacementPairs.put("$rewardAmount", getQuestReward().getRewardMessage());

        return ItemStackSerializer.itemStackPlaceholderReplacer(QuestMenuConfig.killObjectiveButton, placeholderReplacementPairs);

    }

    public boolean isKillAmountQuest() {
        return getQuestType().equals(QuestType.DYNAMIC_KILL_AMOUNT_QUEST);
    }

    public KillAmountQuest getKillAmountQuest() {
        if (!isKillAmountQuest()) return null;
        return (KillAmountQuest) this;
    }

    private void setQuestType(QuestType questType) {
        this.questType = questType;
    }

    public QuestObjective getQuestObjective() {
        return questObjective;
    }

    private void setQuestObjective(QuestObjective questObjective) {
        try {
            this.questObjective = questObjective.clone();
        } catch (Exception ex) {
            new WarningMessage("Failed to clone quest objective! Report this to the dev.");
        }

    }

    public QuestReward getQuestReward() {
        return questReward;
    }

    public void setQuestReward(QuestReward questReward) {
        this.questReward = questReward;
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

}
