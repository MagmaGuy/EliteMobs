package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.PlayerData;
import com.magmaguy.elitemobs.config.menus.premade.QuestMenuConfig;
import com.magmaguy.elitemobs.utils.StringColorAnimator;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.Serializable;

public class QuestObjective implements Serializable {

    private final int questTier;
    private int objectiveKills = 0;
    private int currentKills = 0;
    private int minimumEliteMobTier = 0;
    private int minimumEliteMobLevel = 0;
    private double questDifficulty = 0;
    private final EntityType entityType;
    private boolean isComplete = false;
    private boolean isTurnedIn = false;
    private final QuestReward questReward;

    public QuestObjective(int objectiveKills, int minimumEliteMobTier, EntityType entityType, int questTier) {
        this.questTier = questTier;
        this.objectiveKills = objectiveKills;
        this.minimumEliteMobTier = minimumEliteMobTier;
        this.minimumEliteMobLevel = MobTierCalculator.findMobLevel(getMinimumEliteMobTier());
        this.entityType = entityType;
        this.questDifficulty = questTier * 10 * getObjectiveKills();
        this.questReward = new QuestReward(this.questTier, this.questDifficulty);
    }

    public QuestReward getQuestReward() {
        return this.questReward;
    }

    public int getObjectiveKills() {
        return this.objectiveKills;
    }

    public int getCurrentKills() {
        return currentKills;
    }

    public boolean processQuestProgression(EliteMobEntity eliteMobEntity, Player player) {
        if (!eliteMobEntity.getHasSpecialLoot()) return false;
        if (!eliteMobEntity.getLivingEntity().getType().equals(getEntityType())) return false;
        if (eliteMobEntity.getLevel() < getMinimumEliteMobLevel()) return false;
        addKill(player);
        return true;
    }

    public void addKill(Player player) {
        this.currentKills++;
        if (currentKills >= this.objectiveKills) {
            doQuestCompletion(player);
            questReward.doReward(player);
        }
        sendQuestProgressionMessage(player);
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public int getMinimumEliteMobTier() {
        return minimumEliteMobTier;
    }

    public int getMinimumEliteMobLevel() {
        return minimumEliteMobLevel;
    }

    public double getQuestDifficulty() {
        return this.questDifficulty;
    }

    public boolean isComplete() {
        return isComplete;
    }

    private void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String getEliteMobName() {
        return EliteMobProperties.getPluginData(getEntityType()).getName()
                .replace("$level", MobTierCalculator.findMobLevel(getMinimumEliteMobTier()) + "+");
    }

    public void sendQuestStartMessage(Player player) {
        StringColorAnimator.startTitleAnimation(player, QuestMenuConfig.questStartTitle,
                QuestMenuConfig.questStartSubtitle
                        .replace("$objectiveAmount", getObjectiveKills() + "")
                        .replace("$objectiveName", getEliteMobName())
                , ChatColor.DARK_GREEN, ChatColor.GREEN);
    }

    public void sendQuestCompleteMessage(Player player) {
        if (!player.isOnline()) return;
        StringColorAnimator.startTitleAnimation(player, QuestMenuConfig.questCompleteTitle,
                QuestMenuConfig.questCompleteSubtitle
                        .replace("$objectiveAmount", getObjectiveKills() + "")
                        .replace("$objectiveName", getEliteMobName()),
                ChatColor.GOLD, ChatColor.YELLOW);
    }

    public void sendQuestProgressionMessage(Player player) {
        if (!player.isOnline()) return;
        QuestProgressionBar.sendQuestProgression(player, this);
    }

    public String objectiveString() {
        return QuestMenuConfig.objectiveString
                .replace("$objectiveAmount", getObjectiveKills() + "")
                .replace("$objectiveName", getEliteMobName());
    }

    public boolean isTurnedIn() {
        return isTurnedIn;
    }

    public void setTurnedIn(boolean turnedIn) {
        isTurnedIn = turnedIn;
    }

    public void doQuestCompletion(Player player) {
        if (isTurnedIn) return;
        setTurnedIn(true);
        setComplete(true);
        sendQuestCompleteMessage(player);
        EliteQuest.removePlayersInQuests(player);
        PlayerData.incrementQuestsCompleted(player.getUniqueId());
    }

}
