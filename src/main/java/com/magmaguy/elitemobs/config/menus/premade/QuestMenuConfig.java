package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class QuestMenuConfig extends MenusConfigFields {
    public static String menuName, questSelectorMenuTitle;
    public static ItemStack validTierButton, inactiveTierButton, invalidTierButton;
    public static String cancelMessagePart1, cancelMessagePart2, cancelMessagePart3;
    public static ItemStack killObjectiveButton;
    public static String questCancelMessage, questStartTitle, questStartSubtitle, questCompleteTitle, questCompleteSubtitle,
            objectiveString, rewardMessage, questCompleteBroadcastMessage, questStatusMessage, questStartBroadcastMessage;

    public QuestMenuConfig() {
        super("quest_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        menuName = ConfigurationEngine.setString(file, fileConfiguration, "menuName", "[EM] Quest Selection", true);

        ItemStackSerializer.serialize(
                "validTierButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_STAINED_GLASS_PANE,
                        "&2Take on a $rank &2quest!",
                        new ArrayList<>(List.of(
                                "&aAccept a $rank &aquest and",
                                "&aget special rewards!")), MetadataHandler.signatureID),
                fileConfiguration);
        validTierButton = ItemStackSerializer.deserialize("validTierButton", fileConfiguration);

        cancelMessagePart1 = ConfigurationEngine.setString(file, fileConfiguration, "cancelQuestMessage1",
                "&c&l&m&o---------------------------------------------" +
                        "&cYou can only have one quest at a time! Cancelling your ongoing quest will reset quest progress!", true);
        cancelMessagePart2 = ConfigurationEngine.setString(file, fileConfiguration, "cancelQuestMessage2", "&2[Click here to cancel current quest!]", true);
        cancelMessagePart3 = ConfigurationEngine.setString(file, fileConfiguration, "cancelQuestMessage3",
                "&7You can see your quest status with the command &a/em quest status" +
                        "&c&l&m&o---------------------------------------------", true);

        ItemStackSerializer.serialize(
                "killObjectiveButton",
                ItemStackGenerator.generateItemStack(Material.YELLOW_STAINED_GLASS_PANE,
                        "&2Kill $objectiveAmount $objectiveName",
                        new ArrayList<>(List.of(
                                "&aKill $objectiveAmount $objectiveName &amobs.",
                                "&fProgress: &a$currentAmount &f/&c $objectiveAmount",
                                "&aReward: &e $rewardAmount")), MetadataHandler.signatureID),
                fileConfiguration);
        killObjectiveButton = ItemStackSerializer.deserialize("killObjectiveButton", fileConfiguration);

        questCancelMessage = ConfigurationEngine.setString(file, fileConfiguration, "questCancelMessage", "&7[EliteMobs] &cYour ongoing quest has been cancelled!", true);
        questStartTitle = ConfigurationEngine.setString(file, fileConfiguration, "questStartTitle", "You have accepted a quest!", true);
        questStartSubtitle = ConfigurationEngine.setString(file, fileConfiguration, "questStartSubtitle", "Slay $objectiveAmount $objectiveName", true);
        questCompleteTitle = ConfigurationEngine.setString(file, fileConfiguration, "questCompleteTitle", "Quest complete!", true);
        questCompleteSubtitle = ConfigurationEngine.setString(file, fileConfiguration, "questCompleteSubtitle", "You have slain $objectiveAmount $objectiveName", true);
        objectiveString = ConfigurationEngine.setString(file, fileConfiguration, "bossBarObjective", "Slay $objectiveAmount $objectiveName", true);
        rewardMessage = ConfigurationEngine.setString(file, fileConfiguration, "questRewardMessage", "&7[EM] &aQuest completed! &6Your reward is &2$reward $currencyName", true);
        questCompleteBroadcastMessage = ConfigurationEngine.setString(file, fileConfiguration, "questCompleteBroadcastMessage", "&7[EM] $player &2has completed a $rank &2quest!", true);
        questStartBroadcastMessage = ConfigurationEngine.setString(file, fileConfiguration, "questStartBroadcastMessage", "&7[EM] $player &2has accepted a $rank &2quest!", true);
        questStatusMessage = ConfigurationEngine.setString(file, fileConfiguration, "questStatusMessage", "&7[EM] &aYou've slain &a$currentAmount &f/ &c$objectiveAmount &a$objectiveName", true);
    }
}
