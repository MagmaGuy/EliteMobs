package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class QuestsConfig {

    public static String questJoinMessage, questLeaveMessage, questCompleteMesage, leaveWhenNoActiveQuestsExist, questLeaveConfirmationMessage;

    public static FileConfiguration fileConfiguration;
    private static File file;

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("Quests.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        questJoinMessage = ConfigurationEngine.setString(fileConfiguration, "questJoinMessage", "&aYou have accepted the quest $questName &a!");
        questLeaveMessage = ConfigurationEngine.setString(fileConfiguration, "questLeaveMessage", "&cYou have abandoned the quest $questName &c!");
        questCompleteMesage = ConfigurationEngine.setString(fileConfiguration, "questCompleteMessage", "&2You completed the quest $questName &2!");
        leaveWhenNoActiveQuestsExist = ConfigurationEngine.setString(fileConfiguration, "leaveWhenNoActiveQuestsExist", "&cYou don't currently have an active quest!");
        questLeaveConfirmationMessage = ConfigurationEngine.setString(fileConfiguration, "questLeaveConfirmationMessage", "&cAre you sure you want to abandon your current quest? Do &a/em confirm &c to confirm your choice!");

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }
}
