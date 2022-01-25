package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.items.customloottable.*;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DynamicQuestMenuConfig extends MenusConfigFields {
    @Getter
    private static String questName;
    @Getter
    private static String headerTextLines;
    @Getter
    private static String defaultLoreTextLines;
    @Getter
    private static String acceptTextLines;
    @Getter
    private static String acceptHoverLines;
    @Getter
    private static String acceptCommandLines;
    @Getter
    private static String acceptedTextLines;
    @Getter
    private static String acceptedHoverLines;
    @Getter
    private static String acceptedCommandLines;
    @Getter
    private static String completedTextLines;
    @Getter
    private static String completedHoverLines;
    @Getter
    private static String completedCommandLines;
    @Getter
    private static String turnedInTextLines;
    @Getter
    private static String turnedInHoverLines;
    @Getter
    private static String ongoingColorCode;
    @Getter
    private static String completedColorCode;
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

    public static List<TextComponent> getRewardsDefaultSummaryLine(QuestReward questReward) {
        List<TextComponent> textComponent = new ArrayList<>();
        if (questReward.getCustomLootTable() == null) return textComponent;
        for (CustomLootEntry customLootEntry : questReward.getCustomLootTable().getEntries()) {
            ItemStack itemStack = null;
            if (customLootEntry instanceof EliteCustomLootEntry)
                itemStack = ((EliteCustomLootEntry) customLootEntry).generateItemStack(questReward.getRewardLevel(), Bukkit.getPlayer(questReward.getPlayerUUID()));
            else if (customLootEntry instanceof SpecialCustomLootEntry)
                itemStack = ((SpecialCustomLootEntry) customLootEntry).generateItemStack(Bukkit.getPlayer(questReward.getPlayerUUID()));
            else if (customLootEntry instanceof ItemStackCustomLootEntry)
                itemStack = ((ItemStackCustomLootEntry) customLootEntry).generateItemStack();
            if (itemStack != null) {
                textComponent.add(new TextComponent(rewardsDefaultSummaryLine
                        .replace("$amount", customLootEntry.getAmount() + "")
                        .replace("$rewardName", itemStack.getItemMeta().getDisplayName())
                        .replace("$chance", (int) (customLootEntry.getChance() * 100) + "")));
                continue;
            }

            if (customLootEntry instanceof VanillaCustomLootEntry)
                itemStack = ((VanillaCustomLootEntry) customLootEntry).generateItemStack();
            if (itemStack != null) {
                textComponent.add(new TextComponent(rewardsDefaultSummaryLine
                        .replace("$amount", customLootEntry.getAmount() + "")
                        .replace("$rewardName", itemStack.getType().toString().replace("_", " "))
                        .replace("$chance", (int) (customLootEntry.getChance() * 100) + "")));
                continue;
            }

            if (customLootEntry instanceof CurrencyCustomLootEntry) {
                textComponent.add(new TextComponent(rewardsDefaultSummaryLine
                        .replace("$amount", customLootEntry.getAmount() + "")
                        .replace("$rewardName", ((CurrencyCustomLootEntry) customLootEntry).getCurrencyAmount() + " " + EconomySettingsConfig.getCurrencyName())
                        .replace("$chance", (int) (customLootEntry.getChance() * 100) + "")));

            }
        }
        return textComponent;
    }

    @Override
    public void processAdditionalFields() {
        questName = ConfigurationEngine.setString(fileConfiguration, "questName", "Slay $amount $name");

        headerTextLines = ConfigurationEngine.setString(fileConfiguration, "headerTextLines2",
                ChatColorConverter.convert("&c&lGuild request!\n&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"));

        defaultLoreTextLines = ConfigurationEngine.setString(fileConfiguration, "defaultLoreTextLines", "&8Slay $amount $name!");

        acceptTextLines = ConfigurationEngine.setString(fileConfiguration, "acceptTextLines", "&a&lAccept!");
        acceptHoverLines = ConfigurationEngine.setString(fileConfiguration, "acceptHoverLines", "&aClick to \n&aaccept quest!");
        acceptCommandLines = ConfigurationEngine.setString(fileConfiguration, "acceptCommandLines", "/em quest accept $questID");

        acceptedTextLines = ConfigurationEngine.setString(fileConfiguration, "acceptedTextLines", "&2&lAccepted! &4[Abandon]");
        acceptedHoverLines = ConfigurationEngine.setString(fileConfiguration, "acceptedHoverLines", "&aClick to abandon quest!");
        acceptedCommandLines = ConfigurationEngine.setString(fileConfiguration, "acceptedCommandLines3", "/em quest leave $questID");

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
