package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.items.customloottable.*;
import com.magmaguy.elitemobs.quests.objectives.*;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CustomQuestMenuConfig extends MenusConfigFields {
    @Getter
    private static String headerTextLines;
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
    private static String trackTextLines;
    @Getter
    private static String trackHoverLines;
    @Getter
    private static String trackCommandLines;
    @Getter
    private static String untrackTextLines;
    @Getter
    private static String untrackHoverLines;
    @Getter
    private static String untrackCommandLines;
    @Getter
    private static String completedTextLines;
    @Getter
    private static String completedHoverLines;
    @Getter
    private static String completedCommandLines;
    @Getter
    private static String ongoingColorCode;
    @Getter
    private static String completedColorCode;
    @Getter
    private static String objectivesLine;
    @Getter
    private static String rewardsLine;
    @Getter
    private static String turnedInTextLines;
    private static String turnedInHoverLines;
    private static String killQuestDefaultSummaryLine;
    private static String fetchQuestDefaultSummaryLine;
    private static String dialogQuestDefaultSummaryLine;
    private static String arenaQuestDefaultSummaryLine;
    private static String rewardsDefaultSummaryLine;
    @Getter
    private static boolean useQuestTracking;

    public CustomQuestMenuConfig() {
        super("custom_quest_screen", true);
    }

    public static String getObjectiveLine(Objective objective) {
        if (objective == null) return "";
        String newString = "";
        if (objective instanceof CustomKillObjective)
            newString = killQuestDefaultSummaryLine;
        else if (objective instanceof DialogObjective)
            newString = dialogQuestDefaultSummaryLine.replace("$location", ((DialogObjective) objective).getTargetLocation());
        else if (objective instanceof CustomFetchObjective)
            newString = fetchQuestDefaultSummaryLine;
        else if (objective instanceof ArenaObjective)
            newString = arenaQuestDefaultSummaryLine;
        newString = newString.replace("$name", ChatColor.BLACK + ChatColor.stripColor(objective.getObjectiveName()));
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
                itemStack = ((EliteCustomLootEntry) customLootEntry).generateItemStack(questReward.getRewardLevel(), Bukkit.getPlayer(questReward.getPlayerUUID()), null);
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
        //headerTextLines = ConfigurationEngine.setString(file, fileConfiguration, "headerTextLines", ChatColorConverter.convert("&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n&c&l$questName\n&0&m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"));

        headerTextLines = ConfigurationEngine.setString(file, fileConfiguration, "headerTextLines2", ChatColorConverter.convert("&a&l『&c&l$questName&a&l』\n"), true);

        acceptTextLines = ConfigurationEngine.setString(file, fileConfiguration, "acceptTextLines", "&a&l[Accept!]", true);
        acceptHoverLines = ConfigurationEngine.setString(file, fileConfiguration, "acceptHoverLines", "&aClick to \n&aaccept quest!", true);
        acceptCommandLines = ConfigurationEngine.setString(file, fileConfiguration, "acceptCommandLines", "/em quest accept $questID", false);

        acceptedTextLines = ConfigurationEngine.setString(file, fileConfiguration, "acceptedTextLines", "&2&lAccepted! &aTurn in with $npcName &4[Abandon]", true);
        acceptedHoverLines = ConfigurationEngine.setString(file, fileConfiguration, "acceptedHoverLines", "&aClick to abandon quest!", true);
        acceptedCommandLines = ConfigurationEngine.setString(file, fileConfiguration, "acceptedCommandLines3", "/em quest leave $questID", false);

        useQuestTracking = ConfigurationEngine.setBoolean(fileConfiguration, "useQuestTracking", true);

        trackTextLines = ConfigurationEngine.setString(file, fileConfiguration, "trackTextLines", "&2&l[Track]", true);
        trackHoverLines = ConfigurationEngine.setString(file, fileConfiguration, "trackHoverLines", "&aClick to track quest!", true);
        trackCommandLines = ConfigurationEngine.setString(file, fileConfiguration, "trackCommandLines3", "/em quest track $questID", false);

        untrackTextLines = ConfigurationEngine.setString(file, fileConfiguration, "untrackTextLines", "&4&l[Untrack]", true);
        untrackHoverLines = ConfigurationEngine.setString(file, fileConfiguration, "untrackHoverLines", "&cClick to untrack quest!", true);
        untrackCommandLines = ConfigurationEngine.setString(file, fileConfiguration, "untrackCommandLines", "/em quest track $questID", false);

        completedTextLines = ConfigurationEngine.setString(file, fileConfiguration, "completedTextLines", "&2&l[Turn in!]", true);
        completedHoverLines = ConfigurationEngine.setString(file, fileConfiguration, "completedHoverLines", "&aClick to turn quest in!", true);
        completedCommandLines = ConfigurationEngine.setString(file, fileConfiguration, "completedCommandLines", "/em quest complete $questID", false);

        turnedInTextLines = ConfigurationEngine.setString(file, fileConfiguration, "turnedInTextLines", "&8[Completed!]", true);
        turnedInHoverLines = ConfigurationEngine.setString(file, fileConfiguration, "turnedInHoverLines", "&8Already turned in!", true);

        objectivesLine = ConfigurationEngine.setString(file, fileConfiguration, "objectivesLine", "&c&lObjectives:", true);
        killQuestDefaultSummaryLine = ConfigurationEngine.setString(file, fileConfiguration, "killQuestDefaultSummaryLine", "&c➤Kill $name:$color$current&0/$color$target", true);
        fetchQuestDefaultSummaryLine = ConfigurationEngine.setString(file, fileConfiguration, "fetchQuestDefaultSummaryLine", "&c➤Get $name:$color&$current&0/$color$target", true);
        dialogQuestDefaultSummaryLine = ConfigurationEngine.setString(file, fileConfiguration, "dialogQuestDefaultSummaryLine", "&c➤Go talk to $name $location", true);
        arenaQuestDefaultSummaryLine =  ConfigurationEngine.setString(file, fileConfiguration, "arenaQuestDefaultSummaryLine", "&c➤Complete $arenaName", true);

        rewardsLine = ConfigurationEngine.setString(file, fileConfiguration, "rewardsLine", "&2&lRewards:", true);
        rewardsDefaultSummaryLine = ConfigurationEngine.setString(file, fileConfiguration, "rewardsDefaultSummaryLine", "&2➤$amountx $rewardName &8($chance%)", true);

        ongoingColorCode = ConfigurationEngine.setString(file, fileConfiguration, "ongoingQuestColorCode", "&c", false);
        completedColorCode = ConfigurationEngine.setString(file, fileConfiguration, "ongoingQuestColorCode", "&2", false);

    }

}
