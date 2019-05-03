package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.items.MobTierCalculator;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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

    public boolean processQuestProgression(EliteMobEntity eliteMobEntity) {
        if (!eliteMobEntity.getLivingEntity().getType().equals(getEntityType())) return false;
        if (eliteMobEntity.getLevel() < getMinimumEliteMobTier()) return false;
        addKill();
        return true;
    }

    public void addKill() {
        this.currentKills++;
        if (currentKills >= this.objectiveKills)
            setComplete(true);
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
                .replace("$level", MobTierCalculator.findMobLevel(getMinimumEliteMobTier()) + "&e+&a");
    }

    public void sendQuestCompleteMessage(Player player) {
        if (!player.isOnline()) return;
        player.sendMessage("[EliteMobs] You've completed the a quest!");
    }

    public void sendQuestProgressionMessage(Player player) {
        if (!player.isOnline()) return;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(ChatColorConverter.convert("&7[EM] &a" + getCurrentKills() + "&f/&c" + getObjectiveKills() + " &fkilled")));
    }

}
