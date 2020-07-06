package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class QuestMenuConfig extends MenusConfigFields {
    public QuestMenuConfig() {
        super("quest_menu");
    }

    public static String menuName, questSelectorMenuTitle;
    public static ItemStack validTierButton, inactiveTierButton, invalidTierButton;
    public static String cancelMessagePart1, cancelMessagePart2, cancelMessagePart3;
    public static ItemStack killObjectiveButton;
    public static String questCancelMessage, questStartTitle, questStartSubtitle, questCompleteTitle, questCompleteSubtitle,
            objectiveString, rewardMessage, questCompleteBroadcastMessage, questStatusMessage, questStartBroadcastMessage;

    @Override
    public void generateConfigDefaults(FileConfiguration fileConfiguration) {
        menuName = ConfigurationEngine.setString(fileConfiguration, "menuName", "[EM] Quest Selection");

        ItemStackSerializer.serialize(
                "validTierButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_STAINED_GLASS_PANE,
                        "&2Take on a $rank &2quest!",
                        Arrays.asList(
                                "&aAccept a $rank &aquest and",
                                "&aget special rewards!")),
                fileConfiguration);
        validTierButton = ItemStackSerializer.deserialize("validTierButton", fileConfiguration);

        cancelMessagePart1 = ConfigurationEngine.setString(fileConfiguration, "cancelQuestMessage1",
                "&c&l&m&o---------------------------------------------" +
                        "&cYou can only have one quest at a time! Cancelling your ongoing quest will reset quest progress!");
        cancelMessagePart2 = ConfigurationEngine.setString(fileConfiguration, "cancelQuestMessage2", "&2[Click here to cancel current quest!]");
        cancelMessagePart3 = ConfigurationEngine.setString(fileConfiguration, "cancelQuestMessage3",
                "&7You can see your quest status with the command &a/em quest status" +
                        "&c&l&m&o---------------------------------------------");

        ItemStackSerializer.serialize(
                "killObjectiveButton",
                ItemStackGenerator.generateItemStack(Material.YELLOW_STAINED_GLASS_PANE,
                        "&2Kill $objectiveAmount $objectiveName",
                        Arrays.asList(
                                "&aKill $objectiveAmount $objectiveName &amobs.",
                                "&fProgress: &a$currentAmount &f/&c $objectiveAmount",
                                "&aReward: &e $rewardAmount")),
                fileConfiguration);
        killObjectiveButton = ItemStackSerializer.deserialize("killObjectiveButton", fileConfiguration);

        questCancelMessage = ConfigurationEngine.setString(fileConfiguration, "questCancelMessage", "&7[EliteMobs] &cYour ongoing quest has been cancelled!");
        questStartTitle = ConfigurationEngine.setString(fileConfiguration, "questStartTitle", "You have accepted a quest!");
        questStartSubtitle = ConfigurationEngine.setString(fileConfiguration, "questStartSubtitle", "Slay $objectiveAmount $objectiveName");
        questCompleteTitle = ConfigurationEngine.setString(fileConfiguration, "questCompleteTitle", "Quest complete!");
        questCompleteSubtitle = ConfigurationEngine.setString(fileConfiguration, "questCompleteSubtitle", "You have slain $objectiveAmount $objectiveName");
        objectiveString = ConfigurationEngine.setString(fileConfiguration, "bossBarObjective", "Slay $objectiveAmount $objectiveName");
        rewardMessage = ConfigurationEngine.setString(fileConfiguration, "questRewardMessage", "&7[EM] &aQuest completed! &6Your reward is &2$reward $currencyName");
        questCompleteBroadcastMessage = ConfigurationEngine.setString(fileConfiguration, "questCompleteBroadcastMessage", "&7[EM] $player &2has completed a $rank &2quest!");
        questStartBroadcastMessage = ConfigurationEngine.setString(fileConfiguration, "questStartBroadcastMessage", "&7[EM] $player &2has accepted a $rank &2quest!");
        questStatusMessage = ConfigurationEngine.setString(fileConfiguration, "questStatusMessage", "&7[EM] &aYou've slain &a$currentAmount &f/ &c$objectiveAmount &a$objectiveName");
    }
}
