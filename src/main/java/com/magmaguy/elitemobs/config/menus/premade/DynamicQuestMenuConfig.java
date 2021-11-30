package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import com.magmaguy.elitemobs.quests.rewards.RewardEntry;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DynamicQuestMenuConfig extends MenusConfigFields {
    @Getter
    private static String questName;
    @Getter
    private static String headerTextLines;
    @Getter
    private static String defaultLoreTextLines;
    @Getter
    private static String acceptTextLines, acceptHoverLines, acceptCommandLines;
    @Getter
    private static String acceptedTextLines, acceptedHoverLines, acceptedCommandLines;
    @Getter
    private static String completedTextLines, completedHoverLines, completedCommandLines;
    @Getter
    private static String turnedInTextLines, turnedInHoverLines;
    @Getter
    private static String ongoingColorCode, completedColorCode;
    @Getter
    private static String objectivesLine;
    @Getter
    private static String rewardsLine;

    private static String killQuestDefaultSummaryLine;
    private static String rewardsDefaultSummaryLine;

    public DynamicQuestMenuConfig() {
        super("dynamic_quest_screen", true);
    }

    public static String getKillQuestDefaultSummaryLine(Objective objective) {
        String newString = killQuestDefaultSummaryLine;
        newString = newString.replace("$name", ChatColor.BLACK + ChatColor.stripColor((objective).getObjectiveName()));
        newString = newString.replace("$current", objective.getCurrentAmount() + "");
        newString = newString.replace("$target", objective.getTargetAmount() + "");
        if (!objective.isObjectiveCompleted())
            return newString.replace("$color", ongoingColorCode);
        else
            return newString.replace("$color", completedColorCode);
    }

    public static TextComponent getRewardsDefaultSummaryLine(QuestReward questReward, int questLevel, Player player) {
        TextComponent textComponent = new TextComponent();
        for (RewardEntry rewardEntry : questReward.getRewardEntries())
            if (rewardEntry.getItemStack() != null) {
                TextComponent customItemTextComponent = new TextComponent(rewardsDefaultSummaryLine
                        .replace("$amount", rewardEntry.getAmount() + "")
                        .replace("$rewardName", rewardEntry.getItemStack().getItemMeta().getDisplayName())
                        .replace("$chance", (int) (rewardEntry.getChance() * 100) + ""));
                ShareItem.setItemHoverEvent(customItemTextComponent, rewardEntry.getItemStack());
                textComponent.addExtra(customItemTextComponent);
                textComponent.addExtra("\n");
            } else if (rewardEntry.getCurrencyAmount() != 0) {
                TextComponent customItemTextComponent = new TextComponent(rewardsDefaultSummaryLine
                        .replace("$amount", rewardEntry.getAmount() + "")
                        .replace("$rewardName", rewardEntry.getCurrencyAmount() + " " + EconomySettingsConfig.currencyName)
                        .replace("$chance", (int) (rewardEntry.getChance() * 100) + ""));
                textComponent.addExtra(customItemTextComponent);
                textComponent.addExtra("\n");
            }
        return textComponent;
    }

    @Override
    public void processAdditionalFields() {
        questName = ConfigurationEngine.setString(fileConfiguration, "questName", "Slay $amount $name");

        headerTextLines = ConfigurationEngine.setString(fileConfiguration, "headerTextLines",
                ChatColorConverter.convert("&c&lGuild request!\n&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n"));

        defaultLoreTextLines = ConfigurationEngine.setString(fileConfiguration, "defaultLoreTextLines", "&8Slay $amount $name!");

        acceptTextLines = ConfigurationEngine.setString(fileConfiguration, "acceptTextLines", "&a&lAccept!");
        acceptHoverLines = ConfigurationEngine.setString(fileConfiguration, "acceptHoverLines", "&aClick to \n&aaccept quest!");
        acceptCommandLines = ConfigurationEngine.setString(fileConfiguration, "acceptCommandLines", "/em quest accept $questID");

        acceptedTextLines = ConfigurationEngine.setString(fileConfiguration, "acceptedTextLines", "&2&lAccepted! &4[Abandon]");
        acceptedHoverLines = ConfigurationEngine.setString(fileConfiguration, "acceptedHoverLines", "&aClick to abandon quest!");
        acceptedCommandLines = ConfigurationEngine.setString(fileConfiguration, "acceptedCommandLines2", "/em quest leave");

        completedTextLines = ConfigurationEngine.setString(fileConfiguration, "completedTextLines", "&2&l[Turn in!]");
        completedHoverLines = ConfigurationEngine.setString(fileConfiguration, "completedHoverLines", "&aClick to turn quest in!");
        completedCommandLines = ConfigurationEngine.setString(fileConfiguration, "completedCommandLines", "/em quest complete $questID");

        turnedInTextLines = ConfigurationEngine.setString(fileConfiguration, "turnedInTextLines", "&8[Completed!]");
        turnedInHoverLines = ConfigurationEngine.setString(fileConfiguration, "turnedInHoverLines", "&8Already turned in!");

        objectivesLine = ConfigurationEngine.setString(fileConfiguration, "objectivesLine", "&c&lObjectives:");
        killQuestDefaultSummaryLine = ConfigurationEngine.setString(fileConfiguration, "killQuestDefaultSummaryLine", "&c➤Kill $name:$color$current&0/$color$target");

        rewardsLine = ConfigurationEngine.setString(fileConfiguration, "rewardsLine", "&2&lRewards:");
        rewardsDefaultSummaryLine = ConfigurationEngine.setString(fileConfiguration, "rewardsDefaultSummaryLine", "&2➤$amountx $rewardName &8($chance%)");

        ongoingColorCode = ConfigurationEngine.setString(fileConfiguration, "ongoingQuestColorCode", "&c");
        completedColorCode = ConfigurationEngine.setString(fileConfiguration, "ongoingQuestColorCode", "&2");

    }
}
