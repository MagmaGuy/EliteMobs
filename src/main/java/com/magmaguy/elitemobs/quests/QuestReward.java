package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.QuestMenuConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import org.bukkit.entity.Player;

import java.io.Serializable;

public class QuestReward implements Serializable {

    enum RewardType {
        MONETARY
    }

    private RewardType rewardType;
    private double questReward;
    String rewardMessage;

    public QuestReward(int questTier, double questDifficulty) {
        setRewardType();
        setQuestReward(questTier, questDifficulty);
        setRewardMessage();
    }

    private void setRewardType() {
        this.rewardType = RewardType.MONETARY;
    }

    private RewardType getRewardType() {
        return this.rewardType;
    }

    public double getQuestReward() {
        return this.questReward;
    }

    private void setQuestReward(int questTier, double questDifficulty) {
        if (questTier == 0)
            this.questReward = questDifficulty;
        else
            this.questReward = questDifficulty;
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
            player.sendMessage(QuestMenuConfig.rewardMessage
                    .replace("$reward", questReward + "")
                    .replace("$currencyName", EconomySettingsConfig.currencyName));
        }
    }

}
