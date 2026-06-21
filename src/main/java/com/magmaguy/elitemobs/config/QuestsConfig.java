package com.magmaguy.elitemobs.config;

import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfig;
import com.magmaguy.elitemobs.config.customarenas.CustomArenasConfigFields;
import com.magmaguy.elitemobs.quests.objectives.ArenaObjective;
import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.quests.objectives.DialogObjective;
import com.magmaguy.elitemobs.quests.objectives.KillObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.magmacore.config.ConfigurationFile;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class QuestsConfig extends ConfigurationFile {

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
    private static boolean binaryObjectiveStatusEnabled;
    @Getter
    private static String binaryObjectiveIncompleteSymbol;
    @Getter
    private static String binaryObjectiveCompleteSymbol;
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
    private static String arenaQuestScoreboardProgressionLine;
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
    private static String lowRankDynamicQuestWarning;
    @Getter
    private static String questTurnInObjective;
    @Getter
    private static int horizontalCharacterLimitBedrockMenu;
    @Getter
    private static int itemEntryCharacterLimitBedrockMenu;
    @Getter
    private static String questLockoutSubtitle;
    @Getter
    private static String questLockoutChatMessage;
    @Getter
    private static String invalidQuestIdMessage;
    @Getter
    private static String invalidQuestNpcMessage;
    @Getter
    private static String invalidQuestMessage;
    @Getter
    private static String questTrackingInvalidMessage;
    @Getter
    private static String questTurnInStatus;
    @Getter
    private static String questAcceptedStatus;
    @Getter
    private static String questBackToStatusMenu;
    @Getter
    private static String questAbandonText;
    @Getter
    private static boolean useQuestDialogueBossBars;
    @Getter
    private static boolean hideKeyedBossBarsDuringQuestDialogue;
    @Getter
    private static boolean hideQuestScoreboardDuringQuestDialogue;
    @Getter
    private static int questDialogueCharactersPerLine;
    @Getter
    private static int questDialogueCharactersPerTick;
    @Getter
    private static String questDialogueNamePrefix;
    @Getter
    private static String questDialogueNameSuffix;
    @Getter
    private static String questDialogueLinePrefix;
    @Getter
    private static String questDialogueLineSuffix;
    @Getter
    private static int questDialogueBoxOffsetX;
    @Getter
    private static int questDialogueNameOffsetX;
    @Getter
    private static int questDialogueLineOffsetX;
    @Getter
    private static int questDialoguePromptOffsetX;
    @Getter
    private static String questDialoguePromptText;

    public QuestsConfig() {
        super("Quests.yml");
    }

    private static List<EntityType> setEntityTypes(FileConfiguration fileConfiguration, File file) {
        List<String> entityTypes = new ArrayList<>(new ArrayList<>(List.of(
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
        )));

        List<String> laterEntities = new ArrayList<>(List.of(
                EntityType.HOGLIN.toString(),
                EntityType.ZOGLIN.toString(),
                EntityType.PIGLIN_BRUTE.toString(),
                EntityType.PIGLIN.toString(),
                EntityType.ZOMBIFIED_PIGLIN.toString()));
        entityTypes.addAll(laterEntities);

        ConfigurationEngine.setList(file, fileConfiguration, "questEntityTypes", entityTypes, false);

        List<EntityType> parsedTypes = new ArrayList<>();
        for (String string : entityTypes)
            try {
                parsedTypes.add(EntityType.valueOf(string));
            } catch (Exception ex) {
                Logger.warn("Entity type " + string + " is not a valid entity type from the Spigot API!");
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
        newString = newString.replace("$name", safeObjectiveName(objective));
        newString = newString.replace("$current", objective.getCurrentAmount() + "");
        newString = newString.replace("$target", objective.getTargetAmount() + "");
        if (!objective.isObjectiveCompleted())
            return newString.replace("$color", ongoingColorCode);
        else
            return newString.replace("$color", completedColorCode);
    }

    public static String getQuestScoreboardProgressionLine(Objective objective) {
        if (objective == null) Logger.warn("Objective is null!");

        String newString = "";
        if (objective instanceof KillObjective)
            newString = killQuestScoreboardProgressionLine;
        else if (objective instanceof CustomFetchObjective)
            newString = fetchQuestScoreboardProgressionLine;
        else if (objective instanceof DialogObjective)
            newString = dialogQuestScoreboardProgressionLine;
        else if (objective instanceof ArenaObjective arenaObjective) {
            CustomArenasConfigFields arenaFields = CustomArenasConfig.getCustomArena(arenaObjective.getArenaFilename());
            String arenaDisplayName = arenaFields != null && arenaFields.getArenaName() != null
                    ? arenaFields.getArenaName()
                    : arenaObjective.getArenaFilename();
            newString = arenaQuestScoreboardProgressionLine.replace("$arenaName", arenaDisplayName);
        }
        // Single-step objectives (e.g. "talk to") show a status symbol instead of a meaningless "0/1",
        // and read as a struck-through checklist item once done. Counted objectives keep their x/y.
        if (binaryObjectiveStatusEnabled && objective != null
                && !(objective instanceof ArenaObjective) && objective.getTargetAmount() <= 1) {
            String label = binaryObjectiveLabel(newString);
            String body = (label.isEmpty() ? "" : label + " ") + ChatColor.stripColor(safeObjectiveName(objective));
            if (objective.isObjectiveCompleted())
                return binaryObjectiveCompleteSymbol + " " + completedColorCode + ChatColor.STRIKETHROUGH + body;
            return binaryObjectiveIncompleteSymbol + " " + body;
        }
        newString = newString.replace("$name", safeObjectiveName(objective));
        newString = newString.replace("$current", objective.getCurrentAmount() + "");
        newString = newString.replace("$target", objective.getTargetAmount() + "");
        if (!objective.isObjectiveCompleted())
            return newString.replace("$color", ongoingColorCode);
        else
            return newString.replace("$color", completedColorCode);
    }

    // Extracts the plain verb portion of a scoreboard template (the text before "$name", minus the
    // leading bullet/colour codes), e.g. "&7➤ Talk to &f$name..." -> "Talk to". Keeps translations intact.
    private static String binaryObjectiveLabel(String template) {
        if (template == null) return "";
        int index = template.indexOf("$name");
        String before = index >= 0 ? template.substring(0, index) : template;
        before = ChatColor.stripColor(before).replaceAll("^[^\\p{L}]+", "");
        return before.trim();
    }

    private static String safeObjectiveName(Objective objective) {
        if (objective == null || objective.getObjectiveName() == null) return "";
        String strippedName = ChatColor.stripColor(objective.getObjectiveName());
        return ChatColor.WHITE + (strippedName == null ? "" : strippedName);
    }

    @Override
    public void initializeValues() {

        requireQuestTurnIn = ConfigurationEngine.setBoolean(
                List.of("Sets if quests have to be returned to quest givers to complete the quest."),
                fileConfiguration, "requireQuestTurnIn", true);
        questJoinMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a quest is accepted."),
                file, fileConfiguration, "questJoinMessage", "&aYou have accepted the quest $questName &a!", true);
        questLeaveMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a quest is abandoned."),
                file, fileConfiguration, "questLeaveMessage", "&cYou have abandoned the quest $questName &c!", true);
        questCompleteMessage = ConfigurationEngine.setString(
                List.of("Sets message sent when a quest is completed."),
                file, fileConfiguration, "questCompleteMessage", "&2You completed the quest $questName &2!", true);
        leaveWhenNoActiveQuestsExist = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player tried to abandon a quest when none are active."),
                file, fileConfiguration, "leaveWhenNoActiveQuestsExist", "&cYou don't currently have an active quest!", true);
        useQuestAcceptTitles = ConfigurationEngine.setBoolean(
                List.of("Sets if some of the messages related to quests will use titles instead of chat messages."),
                fileConfiguration, "useQuestAcceptTitles", true);
        questStartTitle = ConfigurationEngine.setString(
                List.of("Sets the title sent when a players starts a quest."),
                file, fileConfiguration, "questStartTitle", "&aQuest Accepted!", true);
        questStartSubtitle = ConfigurationEngine.setString(
                List.of("Sets the subtitle sent when a players starts a quest."),
                file, fileConfiguration, "questStartSubtitle", "$questName", false);
        useQuestCompleteTitles = ConfigurationEngine.setBoolean(
                List.of("Sets if titles will be sent when players complete a quest."),
                fileConfiguration, "useQuestCompleteTitles", true);
        questCompleteTitle = ConfigurationEngine.setString(
                List.of("Sets the title sent when a player completes a quest."),
                file, fileConfiguration, "questCompleteTitle", "&2Quest Completed!", true);
        questCompleteSubtitle = ConfigurationEngine.setString(
                List.of("Sets the subtitle sent when a player completes a quest."),
                file, fileConfiguration, "questCompleteSubtitle", "$questName", true);
        useQuestLeaveTitles = ConfigurationEngine.setBoolean(
                List.of("Sets if titles are sent when players leave quests.."),
                fileConfiguration, "useQuestLeaveTitles", true);
        questLeaveTitle = ConfigurationEngine.setString(
                List.of("Sets the title sent when a player leaves a quest."),
                file, fileConfiguration, "questLeaveTitle", "&cQuest Abandoned!", true);
        questLeaveSubtitle = ConfigurationEngine.setString(
                List.of("Sets the subtitle sent when a player leaves a quest."),
                file, fileConfiguration, "questLeaveSubtitle", "$questName", false);
        doQuestChatProgression = ConfigurationEngine.setBoolean(
                List.of("Sets if messages are sent on chat reporting quest objective progression."),
                fileConfiguration, "doQuestChatProgression", true);
        ongoingColorCode = ConfigurationEngine.setString(
                List.of("Sets the color codes for ongoing objectives."),
                file, fileConfiguration, "ongoingQuestColorCode", "&c", false);
        completedColorCode = ConfigurationEngine.setString(
                List.of("Sets the color codes for completed objectives."),
                file, fileConfiguration, "completedQuestColorCode", "&a", false);
        binaryObjectiveStatusEnabled = ConfigurationEngine.setBoolean(
                List.of("Sets if single-step scoreboard objectives (target of 1, e.g. 'talk to') show a status symbol",
                        "instead of a meaningless '0/1' counter. Completed objectives are shown struck through."),
                fileConfiguration, "binaryObjectiveStatusEnabled", true);
        binaryObjectiveIncompleteSymbol = ConfigurationEngine.setString(
                List.of("Symbol shown before an incomplete single-step objective when binaryObjectiveStatusEnabled is true."),
                file, fileConfiguration, "binaryObjectiveIncompleteSymbol", "&7➤", false);
        binaryObjectiveCompleteSymbol = ConfigurationEngine.setString(
                List.of("Symbol shown before a completed single-step objective when binaryObjectiveStatusEnabled is true."),
                file, fileConfiguration, "binaryObjectiveCompleteSymbol", "&a✔", false);
        killQuestChatProgressionMessage = ConfigurationEngine.setString(
                List.of("Sets the formatting for progression messages of kill quests."),
                file, fileConfiguration, "killQuestChatProgressionMessage", "&8[EliteMobs] &7➤ Kill &f$name&7: $color$current&7/$color$target", true);
        fetchQuestChatProgressionMessage = ConfigurationEngine.setString(
                List.of("Sets the formatting for progression messages of fetch quests."),
                file, fileConfiguration, "fetchQuestChatProgressionMessage", "&8[EliteMobs] &7➤ Get &f$name&7: $color$current&7/$color$target", true);
        dialogQuestChatProgressionMessage = ConfigurationEngine.setString(
                List.of("Sets the formatting for progression messages of dialog quests."),
                file, fileConfiguration, "dialogQuestChatProgressionMessage", "&8[EliteMobs] &7➤ Talk to &f$name&7: $color$current&7/$color$target", true);
        maximumActiveQuests = ConfigurationEngine.setInt(
                List.of("Sets the maximum amount of accepted quests a player can have."),
                fileConfiguration, "maximumActiveQuests", 10);
        questCapMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players when trying to accept an amount of quests that exceed the active quest cap."),
                file, fileConfiguration, "questCapMessage", "&8[EliteMobs] &cYou have reached the maximum amount of active quests (10)! " +
                        "&4Abandon or complete at least one active quest if you want to get more quests!", true);

        useQuestScoreboards = ConfigurationEngine.setBoolean(
                List.of("Sets if scoreboards messages will be used for quests."),
                fileConfiguration, "useQuestScoreboards", true);

        killQuestScoreboardProgressionLine = ConfigurationEngine.setString(
                List.of("Sets the formatting for scoreboard progression messages of kill quests."),
                file, fileConfiguration, "killQuestScoreboardProgressionMessage", "&7➤ Kill &f$name&7: $color$current&7/$color$target", true);
        fetchQuestScoreboardProgressionLine = ConfigurationEngine.setString(
                List.of("Sets the formatting for scoreboard progression message of fetch quests."),
                file, fileConfiguration, "fetchQuestScoreboardProgressionMessage", "&7➤ Get &f$name&7: $color$current&7/$color$target", true);
        dialogQuestScoreboardProgressionLine = ConfigurationEngine.setString(
                List.of("Sets the formatting for scoreboard progression messages of dialog quests."),
                file, fileConfiguration, "dialogQuestScoreboardProgressionMessage", "&7➤ Talk to &f$name&7: $color$current&7/$color$target", true);
        arenaQuestScoreboardProgressionLine = ConfigurationEngine.setString(
                List.of("Sets the formatting for scoreboard progression messages of arena quests."),
                file, fileConfiguration, "arenaQuestScoreboardProgressionMessage", "&7➤ Complete &f$arenaName", true);

        questEntityTypes = setEntityTypes(fileConfiguration, file);

        chatTrackMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players to activate quest tracking."),
                file, fileConfiguration, "chatTrackMessage", "&8[EliteMobs]&2 Click here to track your quest!", true);
        chatTrackHover = ConfigurationEngine.setString(
                List.of("Sets the hover message of the quest tracking message."),
                file, fileConfiguration, "chatTrackHover", "&2Click to track!", true);
        chatTrackCommand = ConfigurationEngine.setString(
                List.of("Sets the command sent when clicking on the tracking message. You really should not modify this."),
                file, fileConfiguration, "chatTrackCommand", "/elitemobs quest track $questID", false);

        chatTrackingMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players when activating quest tracking."),
                file, fileConfiguration, "chatTrackingMessage", "&8[EliteMobs]&9 You are now tracking a quest!", true);
        chatTrackingHover = ConfigurationEngine.setString(
                List.of("Sets the hover message of the quest tracking enabled message."),
                file, fileConfiguration, "chatTrackingHover", "&2Click to untrack/track! /em -> Quests to track a different quest!", true);
        chatTrackingCommand = ConfigurationEngine.setString(
                List.of("Sets the command sent when click on the quest tracking activation message. You really should not modify this."),
                file, fileConfiguration, "chatTrackingCommand", "/elitemobs quest track $questID", false);

        autoTrackQuestsOnAccept = ConfigurationEngine.setBoolean(
                List.of("Sets if quests are automatically tracked when accepted."),
                fileConfiguration, "autoTrackQuestsOnAccept", true);

        noQuestDestinationFound = ConfigurationEngine.setString(
                List.of("Sets the message that appears when the destination of the quest could not be found."),
                file, fileConfiguration, "noQuestDestinationFound", "[EM] No quest destination found!", true);
        questDestinationInOtherWorld = ConfigurationEngine.setString(
                List.of("Sets the message that appears when the quest target is in a different world."),
                file, fileConfiguration, "questDestinationInOtherWorld", "[EM] Go to world $world!", true);

        questAlreadyCompletedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players when trying to accept a quest they have already completed."),
                file, fileConfiguration, "questAlreadyCompletedMessage", "&8[EliteMobs] &cYou already completed this quest!", true);
        questPrerequisitesMissingMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players when trying to accept a quest they do not have the permission for."),
                file, fileConfiguration, "questPrerequisitesMissingMessage", "&8[EliteMobs] &cThis NPC has some quest(s) you can't accept yet!", true);

        lowRankDynamicQuestWarning = ConfigurationEngine.setString(
                List.of("Sets the message sent to players trying to get a quest for which they do not have the correct guild rank activated."),
                file, fileConfiguration, "lowRankDynamicQuestWarning", "&8[EliteMobs] &cYou can't take these quests with your current guild rank! Increase your guild rank to accept these quests.", true);

        questTurnInObjective = ConfigurationEngine.setString(
                List.of("Sets the formatting for the quest turn-in message."),
                file, fileConfiguration, "questTurnInObjective", "&a2Talk to $npcName", true);

        horizontalCharacterLimitBedrockMenu = ConfigurationEngine.setInt(
                List.of("Sets the maximum amount of characters inventory-based menus for quests will have before breaking the line."),
                fileConfiguration, "horizontalCharacterLimitBedrockMenu", 30);
        itemEntryCharacterLimitBedrockMenu = ConfigurationEngine.setInt(
                List.of("Sets the maximum amount of characters per item entry in inventory-based menus for quests before creating another item to continue the entry."),
                fileConfiguration, "itemEntryCharacterLimitBedrockMenu", 300);

        questLockoutSubtitle = ConfigurationEngine.setString(
                List.of("Sets the subtitle shown when a player tries to accept a quest they are locked out from"),
                file, fileConfiguration, "questLockoutSubtitle", "&cQuest Lockout!", true);
        questLockoutChatMessage = ConfigurationEngine.setString(
                List.of("Sets the chat message shown when a player tries to accept a quest they are locked out from",
                        "$questName is the placeholder for the quest name",
                        "$remainingTime is the placeholder for the remaining lockout time"),
                file, fileConfiguration, "questLockoutChatMessage",
                "&c[EliteMobs] &7You completed &c$questName &7recently and must wait another &e$remainingTime &7before you can accept it again.", true);

        invalidQuestIdMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player tries to start a quest with an invalid ID.",
                        "$questId is the placeholder for the quest ID"),
                file, fileConfiguration, "invalidQuestIdMessage",
                "&8[EliteMobs] &cInvalid quest ID for ID $questId", true);
        invalidQuestNpcMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an NPC has an invalid quest configuration."),
                file, fileConfiguration, "invalidQuestNpcMessage",
                "[EliteMobs] This NPC's quest is not valid! This might be a configuration error on the NPC or on the quest.", true);
        invalidQuestMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to admins when a specific quest filename is invalid.",
                        "$quest is the placeholder for the quest filename"),
                file, fileConfiguration, "invalidQuestMessage",
                "Invalid quest: $quest", true);
        questTrackingInvalidMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when quest tracking fails due to an invalid quest ID."),
                file, fileConfiguration, "questTrackingInvalidMessage",
                "[EliteMobs] Failed to get a valid quest with that quest ID!", true);

        questTurnInStatus = ConfigurationEngine.setString(
                List.of("Sets the status suffix shown when a quest is ready to turn in."),
                file, fileConfiguration, "questTurnInStatus", " \u00A7f| \u00A72Turn in!", true);
        questAcceptedStatus = ConfigurationEngine.setString(
                List.of("Sets the status suffix shown when a quest is accepted."),
                file, fileConfiguration, "questAcceptedStatus", " \u00A7f| \u00A7aAccepted", true);
        questBackToStatusMenu = ConfigurationEngine.setString(
                List.of("Sets the text for the back button to return to the status menu."),
                file, fileConfiguration, "questBackToStatusMenu", "\u2190 Back to Status Menu", true);
        questAbandonText = ConfigurationEngine.setString(
                List.of("Sets the text shown on the abandon quest button."),
                file, fileConfiguration, "questAbandonText", "\u00A7l\u00A7c[Abandon]", true);
        useQuestDialogueBossBars = ConfigurationEngine.setBoolean(
                List.of("Experimental: shows quest NPC text through top-anchored boss bars before opening the quest menu."),
                fileConfiguration, "useQuestDialogueBossBars", true);
        hideKeyedBossBarsDuringQuestDialogue = ConfigurationEngine.setBoolean(
                List.of("Experimental: temporarily hides Bukkit keyed boss bars from a player while quest dialogue boss bars are active.",
                        "Anonymous boss bars from other plugins cannot be discovered through Bukkit."),
                fileConfiguration, "hideKeyedBossBarsDuringQuestDialogue", true);
        hideQuestScoreboardDuringQuestDialogue = ConfigurationEngine.setBoolean(
                List.of("Experimental: hides the quest tracking scoreboard and compass boss bar while quest dialogue is active,",
                        "so the dialogue box is not cluttered by the objective list. Restored when the dialogue closes."),
                fileConfiguration, "hideQuestScoreboardDuringQuestDialogue", true);
        questDialogueCharactersPerLine = ConfigurationEngine.setInt(
                List.of("Experimental: maximum visible characters per boss-bar dialogue line before wrapping."),
                fileConfiguration, "questDialogueCharactersPerLine", 48);
        questDialogueCharactersPerTick = ConfigurationEngine.setInt(
                List.of("Experimental: visible characters added per tick while quest dialogue types in."),
                fileConfiguration, "questDialogueCharactersPerTick", 2);
        questDialogueNamePrefix = ConfigurationEngine.setString(
                List.of("Experimental: prefix placed before the NPC name boss-bar title. Use this for custom font offset glyphs."),
                file, fileConfiguration, "questDialogueNamePrefix", "", false);
        questDialogueNameSuffix = ConfigurationEngine.setString(
                List.of("Experimental: suffix placed after the NPC name boss-bar title. Use this for custom font offset glyphs."),
                file, fileConfiguration, "questDialogueNameSuffix", "", false);
        questDialogueLinePrefix = ConfigurationEngine.setString(
                List.of("Experimental: prefix placed before each dialogue line boss-bar title. Use this for custom font offset glyphs."),
                file, fileConfiguration, "questDialogueLinePrefix", "", false);
        questDialogueLineSuffix = ConfigurationEngine.setString(
                List.of("Experimental: suffix placed after each dialogue line boss-bar title. Use this for custom font offset glyphs."),
                file, fileConfiguration, "questDialogueLineSuffix", "", false);
        questDialogueBoxOffsetX = ConfigurationEngine.setInt(
                List.of("Experimental: horizontal pixel shift of the dialogue box image (positive = right).",
                        "Default 0 keeps the box where the art places it (centered in the 256px font canvas)."),
                fileConfiguration, "questDialogueBoxOffsetX", 34);
        questDialogueNameOffsetX = ConfigurationEngine.setInt(
                List.of("Experimental: horizontal pixel shift of the NPC name line (negative = left)."),
                fileConfiguration, "questDialogueNameOffsetX", -80);
        questDialogueLineOffsetX = ConfigurationEngine.setInt(
                List.of("Experimental: horizontal pixel shift of each dialogue body line (negative = left)."),
                fileConfiguration, "questDialogueLineOffsetX", 0);
        questDialoguePromptOffsetX = ConfigurationEngine.setInt(
                List.of("Experimental: horizontal pixel shift of the 'sneak to continue' prompt line (positive = right)."),
                fileConfiguration, "questDialoguePromptOffsetX", 70);
        questDialoguePromptText = ConfigurationEngine.setString(
                List.of("Experimental: text shown on the prompt line of the dialogue box telling players how to advance."),
                file, fileConfiguration, "questDialoguePromptText", "&7&oSneak to continue", true);

    }
}
