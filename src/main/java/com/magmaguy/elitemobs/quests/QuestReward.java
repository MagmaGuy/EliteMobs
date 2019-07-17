package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import org.bukkit.entity.Player;

public class QuestReward {

    enum RewardType {
        MONETARY
    }

    private RewardType rewardType;
    private int questTier;
    private double questDifficulty;
    private double questReward;
    String rewardMessage;

    public QuestReward(int questTier, double questDifficulty) {
        setRewardType();
        setQuestTier(questTier);
        setQuestDifficulty(questDifficulty);
        setQuestReward();
        setRewardMessage();
    }

    private void setRewardType() {
        this.rewardType = RewardType.MONETARY;
    }

    private RewardType getRewardType() {
        return this.rewardType;
    }

    private void setQuestTier(int questTier) {
        this.questTier = questTier;
    }

    private int getQuestTier() {
        return this.questTier;
    }

    private double getQuestDifficulty() {
        return this.questDifficulty;
    }

    private void setQuestDifficulty(double questDifficulty) {
        this.questDifficulty = questDifficulty;
    }

    public double getQuestReward() {
        return this.questReward;
    }

    private void setQuestReward() {
        this.questReward = getQuestTier() * getQuestDifficulty() * 10;
    }

    public String getRewardMessage() {
        return this.rewardMessage;
    }

    private void setRewardMessage() {
        this.rewardMessage = this.questReward + " " + EconomySettingsConfig.currencyName;
    }

    public void doReward(Player player) {
        if (rewardType.equals(RewardType.MONETARY)) {
            EconomyHandler.addCurrency(player.getUniqueId(), questReward);
            player.sendMessage("[EM] You have completed a quest! You reward is " + questReward + " " + EconomySettingsConfig.currencyName);
        }
    }

}
