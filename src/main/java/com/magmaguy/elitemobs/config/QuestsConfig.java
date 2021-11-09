package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class QuestsConfig {

    public static boolean requireQuestTurnIn;
    public static String questJoinMessage;
    public static String questLeaveMessage;
    public static String questCompleteMesage;
    public static String leaveWhenNoActiveQuestsExist;
    public static String questLeaveConfirmationMessage;
    public static boolean useQuestAcceptTitles;
    public static String questStartTitle, questStartSubtitle;
    public static boolean useQuestCompleteTitles;
    public static String questCompleteTitle, questCompleteSubtitle;
    public static boolean useQuestLeaveTitles;
    public static String questLeaveTitle, questLeaveSubtitle;
    public static boolean doQuestChatProgression;
    public static String ongoingColorCode, completedColorCode;
    public static String killQuestChatProgressionMessage, fetchQuestChatProgressionMessage;
    public static boolean showQuestProgressionOnScoreboard;
    private static String killQuestScoreboardProgressionLine, fetchQuestScoreboardProgressionLine;

    public static FileConfiguration fileConfiguration;
    private static File file;

    public static void initializeConfig() {
        file = ConfigurationEngine.fileCreator("Quests.yml");
        fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        requireQuestTurnIn = ConfigurationEngine.setBoolean(fileConfiguration, "requireQuestTurnIn",  true);
        questJoinMessage = ConfigurationEngine.setString(fileConfiguration, "questJoinMessage", "&aYou have accepted the quest $questName &a!");
        questLeaveMessage = ConfigurationEngine.setString(fileConfiguration, "questLeaveMessage", "&cYou have abandoned the quest $questName &c!");
        questCompleteMesage = ConfigurationEngine.setString(fileConfiguration, "questCompleteMessage", "&2You completed the quest $questName &2!");
        leaveWhenNoActiveQuestsExist = ConfigurationEngine.setString(fileConfiguration, "leaveWhenNoActiveQuestsExist", "&cYou don't currently have an active quest!");
        questLeaveConfirmationMessage = ConfigurationEngine.setString(fileConfiguration, "questLeaveConfirmationMessage", "&cAre you sure you want to abandon your current quest? Do &a/em confirm &c to confirm your choice!");
        useQuestAcceptTitles = ConfigurationEngine.setBoolean(fileConfiguration, "useQuestAcceptTitles", true);
        questStartTitle = ConfigurationEngine.setString(fileConfiguration, "questStartTitle", "&aQuest Accepted!");
        questStartSubtitle = ConfigurationEngine.setString(fileConfiguration, "questStartSubtitle", "$questName");
        useQuestCompleteTitles = ConfigurationEngine.setBoolean(fileConfiguration, "useQuestCompleteTitles", true);
        questCompleteTitle = ConfigurationEngine.setString(fileConfiguration, "questCompleteTitle", "&2Quest Completed!");
        questCompleteSubtitle = ConfigurationEngine.setString(fileConfiguration, "questCompleteSubtitle", "$questName");
        useQuestLeaveTitles = ConfigurationEngine.setBoolean(fileConfiguration, "useQuestLeaveTitles", true);
        questLeaveTitle = ConfigurationEngine.setString(fileConfiguration, "questLeaveTitle", "&cQuest Abandoned!");
        questLeaveSubtitle = ConfigurationEngine.setString(fileConfiguration, "questLeaveSubtitle", "$questName");
        doQuestChatProgression = ConfigurationEngine.setBoolean(fileConfiguration, "doQuestChatProgression", true);
        ongoingColorCode = ConfigurationEngine.setString(fileConfiguration, "ongoingQuestColorCode", "&c");
        completedColorCode = ConfigurationEngine.setString(fileConfiguration, "ongoingQuestColorCode", "&2");
        killQuestChatProgressionMessage = ConfigurationEngine.setString(fileConfiguration, "killQuestChatProgressionMessage", "&8[EliteMobs]&c➤Kill $name:$color$current&0/$color$target");
        fetchQuestChatProgressionMessage = ConfigurationEngine.setString(fileConfiguration, "fetchQuestChatProgressionMessage", "&8[EliteMobs]&c➤Get $name:$color$current&0/$color$target");

        killQuestChatProgressionMessage = ConfigurationEngine.setString(fileConfiguration, "killQuestScoreboardProgressionMessage", "&c➤Kill $name:$color$current&0/$color$target");
        fetchQuestChatProgressionMessage = ConfigurationEngine.setString(fileConfiguration, "fetchQuestScoreboardProgressionMessage", "&c➤Get $name:$color$current&0/$color$target");
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

    public static String getKillQuestChatProgressionMessage(Objective objective) {
        String newString = killQuestChatProgressionMessage;
        newString = newString.replace("$name", ChatColor.BLACK + ChatColor.stripColor(((KillObjective) objective).getEntityName()));
        newString = newString.replace("$current", ((KillObjective) objective).getCurrentAmount() + "");
        newString = newString.replace("$target", ((KillObjective) objective).getTargetAmount() + "");
        if (!objective.isObjectiveCompleted())
            return newString.replace("$color", ongoingColorCode);
        else
            return newString.replace("$color", completedColorCode);
    }

    public static String getKillQuestScoreboardProgressionLine(Objective objective) {
        String newString = killQuestChatProgressionMessage;
        newString = newString.replace("$name", ChatColor.BLACK + ChatColor.stripColor(((KillObjective) objective).getEntityName()));
        newString = newString.replace("$current", ((KillObjective) objective).getCurrentAmount() + "");
        newString = newString.replace("$target", ((KillObjective) objective).getTargetAmount() + "");
        if (!objective.isObjectiveCompleted())
            return newString.replace("$color", ongoingColorCode);
        else
            return newString.replace("$color", completedColorCode);
    }
}
