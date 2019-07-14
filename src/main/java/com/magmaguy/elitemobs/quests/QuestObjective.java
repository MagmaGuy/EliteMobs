package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.utils.StringColorAnimator;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class QuestObjective {

    private UUID questUUID;
    private int questTier;
    private int objectiveKills = 0;
    private int currentKills = 0;
    private int minimumEliteMobTier = 0;
    private int minimumEliteMobLevel = 0;
    private double questDifficulty = 0;
    private EntityType entityType;
    private boolean isComplete = false;
    private boolean isTurnedIn = false;
    private QuestReward questReward;

    public QuestObjective(int objectiveKills, int minimumEliteMobTier, EntityType entityType, int questTier) {
        setQuestTier(questTier);
        setObjectiveKills(objectiveKills);
        setMinimumEliteMobTier(minimumEliteMobTier);
        setEntityType(entityType);
        setQuestDifficulty();
    }

    public UUID getQuestUUID() {
        return questUUID;
    }

    public void setQuestUUID(UUID questUUID) {
        this.questUUID = UUID.randomUUID();
    }

    public void setQuestReward(QuestReward questReward) {
        this.questReward = questReward;
    }

    private void setQuestTier(int questTier) {
        this.questTier = questTier;
    }

    public int getQuestTier() {
        return this.questTier;
    }


    private void setObjectiveKills(int objectiveKills) {
        this.objectiveKills = objectiveKills;
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

    public void setCurrentKills(int currentKills) {
        this.currentKills = currentKills;
    }

    private void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    private void setMinimumEliteMobTier(int minimumEliteMobTier) {
        this.minimumEliteMobTier = minimumEliteMobTier;
        setMinimumEliteMobLevel();
    }

    public int getMinimumEliteMobTier() {
        return minimumEliteMobTier;
    }

    public int getMinimumEliteMobLevel() {
        return minimumEliteMobLevel;
    }

    private void setMinimumEliteMobLevel() {
        this.minimumEliteMobLevel = MobTierCalculator.findMobLevel(getMinimumEliteMobTier());
    }

    public double getQuestDifficulty() {
        return this.questDifficulty;
    }

    public void setQuestDifficulty() {
        this.questDifficulty = getObjectiveKills() / 10 * questTier + 5;

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
        StringColorAnimator.startTitleAnimation(player, "You have accepted a quest!",
                "Kill " + getObjectiveKills() + " " + getEliteMobName(), ChatColor.DARK_GREEN, ChatColor.GREEN);
    }

    public void sendQuestCompleteMessage(Player player) {
        if (!player.isOnline()) return;
        StringColorAnimator.startTitleAnimation(player, "Quest complete!",
                "You have killed " + getObjectiveKills() + " " + getEliteMobName(),
                ChatColor.GOLD, ChatColor.YELLOW);
    }

    public void sendQuestProgressionMessage(Player player) {
        if (!player.isOnline()) return;
        QuestProgressionBar.sendQuestProgression(player, this);
    }

    public String objectiveString() {
        return "Kill " + getObjectiveKills() + " " + getEliteMobName();
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
        PlayerQuest.removePlayersInQuests(player);
    }

}
