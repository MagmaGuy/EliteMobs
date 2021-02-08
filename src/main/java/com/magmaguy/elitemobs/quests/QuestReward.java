package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.adventurersguild.GuildRank;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.premade.QuestMenuConfig;
import org.bukkit.entity.Player;

import java.io.Serializable;

public class QuestReward implements Serializable {

    enum RewardType {
        MONETARY,
        ITEM,
        MIXED
    }

    private RewardType rewardType;
    private double questReward;

    public QuestReward(int questTier, double questDifficulty) {
        setRewardType();
        setQuestReward(questTier, questDifficulty);
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
        this.questReward = questDifficulty;
    }

    public String getRewardMessage(Player player) {
        return (this.questReward * GuildRank.currencyBonusMultiplier(GuildRank.getGuildPrestigeRank(player))) + " " + EconomySettingsConfig.currencyName;
    }

    public void doReward(Player player) {
        if (rewardType.equals(RewardType.MONETARY)) {
            EconomyHandler.addCurrency(player.getUniqueId(), this.questReward * GuildRank.currencyBonusMultiplier(GuildRank.getGuildPrestigeRank(player)));
            player.sendMessage(QuestMenuConfig.rewardMessage
                    .replace("$reward", this.questReward * GuildRank.currencyBonusMultiplier(GuildRank.getGuildPrestigeRank(player)) + "")
                    .replace("$currencyName", EconomySettingsConfig.currencyName));
        }
    }

}
