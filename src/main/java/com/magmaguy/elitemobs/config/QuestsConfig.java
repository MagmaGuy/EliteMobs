package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.quests.objectives.DialogObjective;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.utils.VersionChecker;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestsConfig {

    @Getter
    private static boolean requireQuestTurnIn;
    @Getter
    private static String questJoinMessage;
    @Getter
    private static String questLeaveMessage;
    @Getter
    private static String questCompleteMessage;
    @Getter
    private static String leaveWhenNoActiveQuestsExist;
    @Getter
    private static String questLeaveConfirmationMessage;
    @Getter
    private static boolean useQuestAcceptTitles;
    @Getter
    private static String questStartTitle;
    @Getter
    private static String questStartSubtitle;
    @Getter
    private static boolean useQuestCompleteTitles;
    @Getter
    private static String questCompleteTitle;
    @Getter
    private static String questCompleteSubtitle;
    @Getter
    private static boolean useQuestLeaveTitles;
    @Getter
    private static String questLeaveTitle;
    @Getter
    private static String questLeaveSubtitle;
    @Getter
    private static boolean doQuestChatProgression;
    @Getter
    private static String ongoingColorCode;
    @Getter
    private static String completedColorCode;
    @Getter
    private static String killQuestChatProgressionMessage;
    @Getter
    private static String fetchQuestChatProgressionMessage;
    @Getter
    private static String dialogQuestChatProgressionMessage;
    @Getter
    private static boolean useQuestScoreboards;
    @Getter
    private static String killQuestScoreboardProgressionLine;
    @Getter
    private static String fetchQuestScoreboardProgressionLine;
    @Getter
    private static String dialogQuestScoreboardProgressionLine;
    @Getter
    private static int maximumActiveQuests;
    @Getter
    private static String questCapMessage;
    @Getter
    private static List<EntityType> questEntityTypes = new ArrayList<>();
    @Getter
    private static String chatTrackMessage;
    @Getter
    private static String chatTrackHover;
    @Getter
    private static String chatTrackCommand;
    @Getter
    private static String chatTrackingMessage;
    @Getter
    private static String chatTrackingHover;
    @Getter
    private static String chatTrackingCommand;
    @Getter
    private static boolean autoTrackQuestsOnAccept;
    @Getter
    private static String noQuestDestinationFound;
    @Getter
    private static String questDestinationInOtherWorld;
    @Getter
    private static String questAlreadyCompletedMessage;
    @Getter
    private static String questPrerequisitesMissingMessage;
    @Getter
    private static int maxCharactersPerBookPage;
    @Getter
    private static String lowRankDynamicQuestWarning;
    @Getter
    private static String questTurnInObjective;
    @Getter
    private static int horizontalCharacterLimitBedrockMenu;
    @Getter
    private static int itemEntryCharacterLimitBedrockMenu;

    private QuestsConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("Quests.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        requireQuestTurnIn = ConfigurationEngine.setBoolean(fileConfiguration, "requireQuestTurnIn", true);
        questJoinMessage = ConfigurationEngine.setString(file, fileConfiguration, "questJoinMessage", "&aYou have accepted the quest $questName &a!", true);
        questLeaveMessage = ConfigurationEngine.setString(file, fileConfiguration, "questLeaveMessage", "&cYou have abandoned the quest $questName &c!", true);
        questCompleteMessage = ConfigurationEngine.setString(file, fileConfiguration, "questCompleteMessage", "&2You completed the quest $questName &2!", true);
        leaveWhenNoActiveQuestsExist = ConfigurationEngine.setString(file, fileConfiguration, "leaveWhenNoActiveQuestsExist", "&cYou don't currently have an active quest!", true);
        questLeaveConfirmationMessage = ConfigurationEngine.setString(file, fileConfiguration, "questLeaveConfirmationMessage", "&cAre you sure you want to abandon your current quest? Do &a/em confirm &c to confirm your choice!", true);
        useQuestAcceptTitles = ConfigurationEngine.setBoolean(fileConfiguration, "useQuestAcceptTitles", true);
        questStartTitle = ConfigurationEngine.setString(file, fileConfiguration, "questStartTitle", "&aQuest Accepted!", true);
        questStartSubtitle = ConfigurationEngine.setString(file, fileConfiguration, "questStartSubtitle", "$questName", false);
        useQuestCompleteTitles = ConfigurationEngine.setBoolean(fileConfiguration, "useQuestCompleteTitles", true);
        questCompleteTitle = ConfigurationEngine.setString(file, fileConfiguration, "questCompleteTitle", "&2Quest Completed!", true);
        questCompleteSubtitle = ConfigurationEngine.setString(file, fileConfiguration, "questCompleteSubtitle", "$questName", false);
        useQuestLeaveTitles = ConfigurationEngine.setBoolean(fileConfiguration, "useQuestLeaveTitles", true);
        questLeaveTitle = ConfigurationEngine.setString(file, fileConfiguration, "questLeaveTitle", "&cQuest Abandoned!", true);
        questLeaveSubtitle = ConfigurationEngine.setString(file, fileConfiguration, "questLeaveSubtitle", "$questName", false);
        doQuestChatProgression = ConfigurationEngine.setBoolean(fileConfiguration, "doQuestChatProgression", true);
        ongoingColorCode = ConfigurationEngine.setString(file, fileConfiguration, "ongoingQuestColorCode", "&c", false);
        completedColorCode = ConfigurationEngine.setString(file, fileConfiguration, "ongoingQuestColorCode", "&2", false);
        killQuestChatProgressionMessage = ConfigurationEngine.setString(file, fileConfiguration, "killQuestChatProgressionMessage", "&8[EliteMobs]&c➤Kill $name:$color$current&0/$color$target", true);
        fetchQuestChatProgressionMessage = ConfigurationEngine.setString(file, fileConfiguration, "fetchQuestChatProgressionMessage", "&8[EliteMobs]&c➤Get $name:$color$current&0/$color$target", true);
        dialogQuestChatProgressionMessage = ConfigurationEngine.setString(file, fileConfiguration, "dialogQuestChatProgressionMessage", "&8[EliteMobs]&c➤Talk to $name:$color$current&0/$color$target", true);
        maximumActiveQuests = ConfigurationEngine.setInt(fileConfiguration, "maximumActiveQuests", 10);
        questCapMessage = ConfigurationEngine.setString(file, fileConfiguration, "questCapMessage", "&8[EliteMobs] &cYou have reached the maximum amount of active quests (10)! " +
                "&4Abandon or complete at least one active quest if you want to get more quests!", true);

        useQuestScoreboards = ConfigurationEngine.setBoolean(fileConfiguration, "useQuestScoreboards", true);

        killQuestScoreboardProgressionLine = ConfigurationEngine.setString(file, fileConfiguration, "killQuestScoreboardProgressionMessage", "&c➤Kill $name:$color$current&0/$color$target", true);
        fetchQuestScoreboardProgressionLine = ConfigurationEngine.setString(file, fileConfiguration, "fetchQuestScoreboardProgressionMessage", "&c➤Get $name:$color$current&0/$color$target", true);
        dialogQuestScoreboardProgressionLine = ConfigurationEngine.setString(file, fileConfiguration, "dialogQuestScoreboardProgressionMessage", "&c➤Talk to $name:$color$current&0/$color$target", true);

        questEntityTypes = setEntityTypes(fileConfiguration, file);

        chatTrackMessage = ConfigurationEngine.setString(file, fileConfiguration, "chatTrackMessage", "&8[EliteMobs]&2 Click here to track your quest!", true);
        chatTrackHover = ConfigurationEngine.setString(file, fileConfiguration, "chatTrackHover", "&2Click to track!", true);
        chatTrackCommand = ConfigurationEngine.setString(file, fileConfiguration, "chatTrackCommand", "/elitemobs quest track $questID", false);

        chatTrackingMessage = ConfigurationEngine.setString(file, fileConfiguration, "chatTrackingMessage", "&8[EliteMobs]&9 You are now tracking a quest!", true);
        chatTrackingHover = ConfigurationEngine.setString(file, fileConfiguration, "chatTrackingHover", "&2Click to untrack/track! /em -> Quests to track a different quest!", true);
        chatTrackingCommand = ConfigurationEngine.setString(file, fileConfiguration, "chatTrackingCommand", "/elitemobs quest track $questID", false);

        autoTrackQuestsOnAccept = ConfigurationEngine.setBoolean(fileConfiguration, "autoTrackQuestsOnAccept", true);

        noQuestDestinationFound = ConfigurationEngine.setString(file, fileConfiguration, "noQuestDestinationFound", "[EM] No quest destination found!", true);
        questDestinationInOtherWorld = ConfigurationEngine.setString(file, fileConfiguration, "questDestinationInOtherWorld", "[EM] Go to world $world!", true);

        questAlreadyCompletedMessage = ConfigurationEngine.setString(file, fileConfiguration, "questAlreadyCompletedMessage", "&8[EliteMobs] &cYou already completed this quest!", true);
        questPrerequisitesMissingMessage = ConfigurationEngine.setString(file, fileConfiguration, "questPrerequisitesMissingMessage", "&8[EliteMobs] &cCan't accept this quest yet!", true);

        maxCharactersPerBookPage = ConfigurationEngine.setInt(fileConfiguration, "maxCharactersPerBookPage", 30);
        lowRankDynamicQuestWarning = ConfigurationEngine.setString(file, fileConfiguration, "lowRankDynamicQuestWarning", "&8[EliteMobs] &cYou can't take these quests with your current guild rank! Increase your guild rank to accept these quests.", true);

        questTurnInObjective = ConfigurationEngine.setString(file, fileConfiguration, "questTurnInObjective", "&a2Talk to $npcName", true);

        horizontalCharacterLimitBedrockMenu = ConfigurationEngine.setInt(fileConfiguration, "horizontalCharacterLimitBedrockMenu", 30);
        itemEntryCharacterLimitBedrockMenu = ConfigurationEngine.setInt(fileConfiguration, "itemEntryCharacterLimitBedrockMenu", 300);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

    private static List<EntityType> setEntityTypes(FileConfiguration fileConfiguration, File file) {
        List<String> entityTypes = new ArrayList<>(Arrays.asList(
                EntityType.BLAZE.toString(),
                EntityType.CAVE_SPIDER.toString(),
                EntityType.DROWNED.toString(),
                EntityType.ELDER_GUARDIAN.toString(),
                EntityType.ENDERMAN.toString(),
                EntityType.ENDERMITE.toString(),
                EntityType.EVOKER.toString(),
                EntityType.GHAST.toString(),
                EntityType.GUARDIAN.toString(),
                EntityType.HUSK.toString(),
                EntityType.ILLUSIONER.toString(),
                EntityType.IRON_GOLEM.toString(),
                EntityType.PILLAGER.toString(),
                EntityType.RAVAGER.toString(),
                EntityType.SILVERFISH.toString(),
                EntityType.SKELETON.toString(),
                EntityType.SPIDER.toString(),
                EntityType.STRAY.toString(),
                EntityType.VINDICATOR.toString(),
                EntityType.WITCH.toString(),
                EntityType.WITHER_SKELETON.toString(),
                EntityType.WOLF.toString(),
                EntityType.ZOMBIE.toString()
        ));

        if (!VersionChecker.serverVersionOlderThan(16, 0)) {
            List<String> laterEntities = Arrays.asList(
                    EntityType.HOGLIN.toString(),
                    EntityType.ZOGLIN.toString(),
                    EntityType.PIGLIN_BRUTE.toString(),
                    EntityType.PIGLIN.toString(),
                    EntityType.ZOMBIFIED_PIGLIN.toString());
            entityTypes.addAll(laterEntities);
        }

        ConfigurationEngine.setList(file, fileConfiguration, "questEntityTypes", entityTypes, false);

        List<EntityType> parsedTypes = new ArrayList<>();
        for (String string : entityTypes)
            try {
                parsedTypes.add(EntityType.valueOf(string));
            } catch (Exception ex) {
                new WarningMessage("Entity type " + string + " is not a valid entity type from the Spigot API!");
            }
        return parsedTypes;
    }

    public static String getQuestChatProgressionMessage(Objective objective) {
        String newString = "";
        if (objective instanceof KillObjective)
            newString = killQuestChatProgressionMessage;
        else if (objective instanceof CustomFetchObjective)
            newString = fetchQuestChatProgressionMessage;
        else if (objective instanceof DialogObjective)
            newString = dialogQuestChatProgressionMessage;
        newString = newString.replace("$name", ChatColor.WHITE + ChatColor.stripColor(objective.getObjectiveName()));
        newString = newString.replace("$current", objective.getCurrentAmount() + "");
        newString = newString.replace("$target", objective.getTargetAmount() + "");
        if (!objective.isObjectiveCompleted())
            return newString.replace("$color", ongoingColorCode);
        else
            return newString.replace("$color", completedColorCode);
    }

    public static String getQuestScoreboardProgressionLine(Objective objective) {
        String newString = "";
        if (objective instanceof KillObjective)
            newString = killQuestScoreboardProgressionLine;
        else if (objective instanceof CustomFetchObjective)
            newString = fetchQuestScoreboardProgressionLine;
        else if (objective instanceof DialogObjective)
            newString = dialogQuestScoreboardProgressionLine;
        newString = newString.replace("$name", ChatColor.WHITE + ChatColor.stripColor(objective.getObjectiveName()));
        newString = newString.replace("$current", objective.getCurrentAmount() + "");
        newString = newString.replace("$target", objective.getTargetAmount() + "");
        if (!objective.isObjectiveCompleted())
            return newString.replace("$color", ongoingColorCode);
        else
            return newString.replace("$color", completedColorCode);
    }
}
