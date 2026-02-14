package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

public class DungeonsConfig extends ConfigurationFile {

    @Getter
    private static String instancedDungeonTitle;
    @Getter
    private static List<String> instancedDungeonDescription;
    @Getter
    private static String dungeonJoinAsPlayerText;
    @Getter
    private static String dungeonJoinAsSpectatorText;
    @Getter
    private static boolean allowSpectatorsInInstancedContent;
    @Getter
    private static String instancedDungeonCompleteMessage;
    @Getter
    private static String instancedDungeonClosingInstanceMessage;
    @Getter
    private static boolean friendlyFireInDungeons;
    @Getter
    private static double fireDamageMultiplier;
    @Getter
    private static double poisonDamageMultiplier;
    @Getter
    private static double witherDamageMultiplier;
    @Getter
    private static String dynamicDungeonLevelSelectionMenuTitle;
    @Getter
    private static String dynamicDungeonLevelSelectionItemTitle;
    @Getter
    private static String dynamicDungeonLevelSelectionClickToSelect;
    @Getter
    private static String dynamicDungeonLevelSelectionMobsWillBeLevel;
    @Getter
    private static String dynamicDungeonLevelSelectionRecommended;
    @Getter
    private static String dynamicDungeonLevelSelectionEasier;
    @Getter
    private static String dynamicDungeonLevelSelectionHarder;
    @Getter
    private static String dynamicDungeonDifficultySelectionMenuTitle;
    @Getter
    private static String dynamicDungeonDifficultySelectionSelectedLevel;
    @Getter
    private static String dungeonLockoutTitle;
    @Getter
    private static String dungeonLockoutSubtitle;
    @Getter
    private static String dungeonLockoutChatMessage;
    @Getter
    private static String invalidInstancedDungeonMessage;
    @Getter
    private static String invalidDynamicDungeonMessage;
    @Getter
    private static String matchAlreadyEndedMessage;
    @Getter
    private static String dungeonBrowserPlayersLabel;
    @Getter
    private static String dungeonBrowserPlayerEntryFormat;
    @Getter
    private static String dungeonBrowserLevelLabel;

    // WorldOperationQueue messages
    @Getter
    private static String dungeonPreparingQueueMessage;
    @Getter
    private static String dungeonPreparingMessage;
    @Getter
    private static String dungeonPrepareFailedMessage;
    @Getter
    private static String dungeonLoadFailedMessage;
    @Getter
    private static String dungeonCancelledShutdownMessage;

    // DungeonInstance messages
    @Getter
    private static String dungeonDataFailedMessage;
    @Getter
    private static String dungeonNoPermissionMessage;
    @Getter
    private static String dungeonCancelledEventMessage;
    @Getter
    private static String dungeonCopyFailedMessage;
    @Getter
    private static String dungeonWorldLoadFailedMessage;
    @Getter
    private static String dungeonDifficultyMessage;

    // DynamicDungeonInstance messages
    @Getter
    private static String dynamicDungeonDataFailedMessage;
    @Getter
    private static String dynamicDungeonNoPermissionMessage;
    @Getter
    private static String dynamicDungeonWorldLoadFailedMessage;
    @Getter
    private static String dynamicDungeonLevelSetMessage;

    // EnchantmentDungeonInstance messages
    @Getter
    private static String enchantNoChallengeMessage;
    @Getter
    private static String enchantChallengeCompleteMessage;
    @Getter
    private static String enchantChallengeSuccessMessage;
    @Getter
    private static String enchantCriticalFailureMessage;
    @Getter
    private static String enchantChallengeFailedMessage;

    // WorldPackage messages
    @Getter
    private static String contentInstalledMessage;
    @Getter
    private static String contentUninstallFailedMessage;
    @Getter
    private static String contentUninstalledMessage;

    // DynamicDungeonPackage messages
    @Getter
    private static String dynamicDungeonAccessMessage1;
    @Getter
    private static String dynamicDungeonAccessMessage2;
    @Getter
    private static String dynamicDungeonAccessMessage3;

    // Setup menu tooltips (EMPackage)
    @Getter
    private static String contentInstalledLine1;
    @Getter
    private static String contentInstalledLine2;
    @Getter
    private static String contentNotInstalledLine1;
    @Getter
    private static String contentNotInstalledLine2;
    @Getter
    private static String contentPartialLine1;
    @Getter
    private static String contentPartialLine2;
    @Getter
    private static String contentPartialLine3;
    @Getter
    private static String contentPartialLine4;
    @Getter
    private static String contentNotDownloadedLine1;
    @Getter
    private static String contentNotDownloadedLine2;
    @Getter
    private static String contentNeedAccessMessage;
    @Getter
    private static String contentAccessClickMessage;
    @Getter
    private static String contentAvailablePatreon;
    @Getter
    private static String contentAvailableItch;
    @Getter
    private static String contentUpdateAvailable;
    @Getter
    private static String contentUpdateClickMessage;
    @Getter
    private static String contentUpdateNeedAccess;
    @Getter
    private static String contentUpdateAccessClick;

    // Download messages (EMPackage)
    @Getter
    private static String contentDownloadLegacyMessage;
    @Getter
    private static String contentDownloadSeparator;
    @Getter
    private static String contentNightbreakPromptLine1;
    @Getter
    private static String contentNightbreakPromptLine2;
    @Getter
    private static String contentNightbreakPromptLine3;
    @Getter
    private static String contentNightbreakPromptLine4;
    @Getter
    private static String contentCheckingAccessMessage;
    @Getter
    private static String contentAccessFailedMessage;
    @Getter
    private static String contentAccessFailedLink;
    @Getter
    private static String contentDownloadedReloadMessage;

    // WorldInstancedDungeonPackage messages
    @Getter
    private static String instancedDungeonInstalledMessage;
    @Getter
    private static String instancedDungeonAccessMessage;
    @Getter
    private static String instancedDungeonInstallNote;
    @Getter
    private static String enchantmentDungeonInstalledMessage;

    // InstanceDeathLocation messages
    @Getter
    private static String instancePunchToRez;
    @Getter
    private static String instanceLivesLeft;

    // SkillSystemMigration messages
    @Getter
    private static String skillMigrationSeparator;
    @Getter
    private static String skillMigrationTitle;
    @Getter
    private static String skillMigrationLine1;
    @Getter
    private static String skillMigrationLine2;
    @Getter
    private static String skillMigrationLine3;
    @Getter
    private static String skillMigrationLine4;
    @Getter
    private static String skillMigrationLine5;
    @Getter
    private static String skillMigrationLine6;

    // DynamicDungeonPackage install message
    @Getter
    private static String dynamicDungeonInstalledMessage;

    // VorpalStrikeSkill messages
    @Getter
    private static String vorpalStrikeReadyMessage;

    // SoulSiphonSkill messages
    @Getter
    private static String soulSiphonDecayMessage;

    // SpectralScytheSkill messages
    @Getter
    private static String spectralScytheCooldownMessage;

    // SkillXPHandler messages
    @Getter
    private static String skillLevelUpTitle;
    @Getter
    private static String skillLevelUpSubtitle;
    @Getter
    private static String skillLevelUpBroadcast;
    @Getter
    private static String skillXpNoGainMessage;
    @Getter
    private static String skillXpCappedMessage;

    // SkillBonus messages
    @Getter
    private static String skillActivationFormat;
    @Getter
    private static String skillStackFormat;

    // Taze messages
    @Getter
    private static String tazeShockedTitle;
    @Getter
    private static String tazeShockedSubtitle;

    // LegionsDisciplineSkill messages
    @Getter
    private static String legionsDisciplineBrokenMessage;

    // AvatarOfJudgmentSkill messages
    @Getter
    private static String avatarFadesMessage;

    // ModelsPackage messages
    @Getter
    private static String modelsAutoInstallMessage;
    @Getter
    private static String modelsCannotUninstallMessage;

    // ItemsPackage messages
    @Getter
    private static String itemsInstallingMessage;
    @Getter
    private static String itemsUninstallingMessage;
    @Getter
    private static String itemsSavingMessage;
    @Getter
    private static String itemsReloadingMessage;

    // EventsPackage messages
    @Getter
    private static String eventsInstallingMessage;
    @Getter
    private static String eventsUninstallingMessage;
    @Getter
    private static String eventsSavingMessage;
    @Getter
    private static String eventsReloadingMessage;

    // Access info messages (EMPackage)
    @Getter
    private static String contentNoAccessMessage;
    @Getter
    private static String contentGetAccessMessage;
    @Getter
    private static String contentNightbreakLink;
    @Getter
    private static String contentPatreonLink;
    @Getter
    private static String contentItchLink;
    @Getter
    private static String contentLinkAccountMessage;

    public DungeonsConfig() {
        super("dungeons.yml");
    }

    @Override
    public void initializeValues() {

        dungeonJoinAsPlayerText = ConfigurationEngine.setString(
                List.of("Sets the text for joining a dungeon as a player! Placeholders:", "$dungeonName - the name of the dungeon"),
                file, fileConfiguration, "joinDungeonAsPlayerText", "&fJoin $dungeonName as a player!", true);
        dungeonJoinAsSpectatorText = ConfigurationEngine.setString(
                List.of("Sets the text for joining a dungeon as a spectator! Placeholders:", "$dungeonName - the name of the dungeon"),
                file, fileConfiguration, "joinDungeonAsSpectatorText", "&fJoin $dungeonName as a spectator!", true);
        instancedDungeonTitle = ConfigurationEngine.setString(
                List.of(
                        "Sets the title that will show up in the item description of instanced dungeon menus",
                        "$difficulty is the placeholder for the difficulty name in the configuration file of the dungeon"),
                file, fileConfiguration, "instancedDungeonTitle", "Start $difficulty difficulty dungeon!",
                true);
        instancedDungeonDescription = ConfigurationEngine.setList(
                List.of("Sets the description that will show up in the item description of instanced dungeon menus",
                        "$dungeonName is the placeholder for the dungeon name in the configuration file of the dungeon"),
                file,
                fileConfiguration,
                "instancedDungeonDescription",
                List.of("&fCreate a new instance of the dungeon", "$dungeonName &ffor yourself and maybe", "&fsome friends!"),
                true);
        allowSpectatorsInInstancedContent = ConfigurationEngine.setBoolean(
                List.of("Sets is spectating instanced content will be available."),
                fileConfiguration, "allowSpectatorsInInstancedContent", true);
        instancedDungeonCompleteMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when an instanced dungeon is completed"),
                file, fileConfiguration, "instancedDungeonCompleteMessage", "[EliteMobs] Dungeon completed! It will self-destruct in 2 minutes!",
                true);
        instancedDungeonClosingInstanceMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when an instanced dungeon closing"),
                file, fileConfiguration, "instancedDungeonClosingInstanceMessage", "[EliteMobs] Closing instance!",
                true);
        friendlyFireInDungeons = ConfigurationEngine.setBoolean(
                List.of("Sets if PvP will be allowed in dungeons"),
                fileConfiguration, "friendlyFireInDungeons", false);
        fireDamageMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the damage multiplier for fire damage in dungeons",
                        "This is important for balance as by default the damage is a bit too high for the dungeons as we design them"),
                fileConfiguration, "fireDamageMultiplier", 0.5);
        witherDamageMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the damage multiplier for wither damage in dungeons",
                        "This is important for balance as by default the damage is a bit too high for the dungeons as we design them"),
                fileConfiguration, "witherDamageMultiplier", 0.5);
        poisonDamageMultiplier = ConfigurationEngine.setDouble(
                List.of("Sets the damage multiplier for fire damage in dungeons",
                        "This is important for balance as by default the damage is a bit too high for the dungeons as we design them"),
                fileConfiguration, "poisonDamageMultiplier", 0.5);
        dynamicDungeonLevelSelectionMenuTitle = ConfigurationEngine.setString(
                List.of("Sets the title for the dynamic dungeon level selection menu"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionMenuTitle", "&8Select Dungeon Level", true);
        dynamicDungeonLevelSelectionItemTitle = ConfigurationEngine.setString(
                List.of("Sets the title for level selection items in the dynamic dungeon menu",
                        "$level is the placeholder for the level number"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionItemTitle", "&fLevel $level", true);
        dynamicDungeonLevelSelectionClickToSelect = ConfigurationEngine.setString(
                List.of("Sets the 'click to select' text in the dynamic dungeon level selection menu",
                        "$level is the placeholder for the level number"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionClickToSelect", "&7Click to select level &e$level", true);
        dynamicDungeonLevelSelectionMobsWillBeLevel = ConfigurationEngine.setString(
                List.of("Sets the 'mobs will be level' text in the dynamic dungeon level selection menu",
                        "$level is the placeholder for the level number"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionMobsWillBeLevel", "&7All mobs will be level &e$level", true);
        dynamicDungeonLevelSelectionRecommended = ConfigurationEngine.setString(
                List.of("Sets the 'recommended level' text in the dynamic dungeon level selection menu"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionRecommended", "&a&lRecommended level!", true);
        dynamicDungeonLevelSelectionEasier = ConfigurationEngine.setString(
                List.of("Sets the 'easier than recommended' text in the dynamic dungeon level selection menu"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionEasier", "&eEasier than recommended", true);
        dynamicDungeonLevelSelectionHarder = ConfigurationEngine.setString(
                List.of("Sets the 'harder than recommended' text in the dynamic dungeon level selection menu"),
                file, fileConfiguration, "dynamicDungeonLevelSelectionHarder", "&6Harder than recommended", true);
        dynamicDungeonDifficultySelectionMenuTitle = ConfigurationEngine.setString(
                List.of("Sets the title for the dynamic dungeon difficulty selection menu"),
                file, fileConfiguration, "dynamicDungeonDifficultySelectionMenuTitle", "&8Select Difficulty", true);
        dynamicDungeonDifficultySelectionSelectedLevel = ConfigurationEngine.setString(
                List.of("Sets the 'selected level' text in the dynamic dungeon difficulty selection menu",
                        "$level is the placeholder for the selected level number"),
                file, fileConfiguration, "dynamicDungeonDifficultySelectionSelectedLevel", "&7Selected Level: &e$level", true);
        dungeonLockoutTitle = ConfigurationEngine.setString(
                List.of("Title shown on screen during dungeon boss lockout.",
                        "Leave empty for subtitle only."),
                file, fileConfiguration, "dungeonLockoutTitle", "", true);
        dungeonLockoutSubtitle = ConfigurationEngine.setString(
                List.of("Sets the subtitle shown when a player kills a boss they are locked out from"),
                file, fileConfiguration, "dungeonLockoutSubtitle", "&cLockout!", true);
        dungeonLockoutChatMessage = ConfigurationEngine.setString(
                List.of("Sets the chat message shown when a player kills a boss they are locked out from",
                        "$bossName is the placeholder for the boss name",
                        "$remainingTime is the placeholder for the remaining lockout time"),
                file, fileConfiguration, "dungeonLockoutChatMessage",
                "&c[EliteMobs] &7You killed &c$bossName &7in the last lockout period and must wait another &e$remainingTime &7before it will drop loot for you again.", true);
        invalidInstancedDungeonMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player tries to open a browser for an invalid instanced dungeon"),
                file, fileConfiguration, "invalidInstancedDungeonMessage", "[EliteMobs] Not a valid instanced dungeon!", true);
        invalidDynamicDungeonMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player tries to open a browser for an invalid dynamic dungeon"),
                file, fileConfiguration, "invalidDynamicDungeonMessage", "[EliteMobs] Not a valid dynamic dungeon!", true);
        matchAlreadyEndedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player tries to join a match that has already ended"),
                file, fileConfiguration, "matchAlreadyEndedMessage", "[EliteMobs] This match already ended! Can't join it!", true);
        dungeonBrowserPlayersLabel = ConfigurationEngine.setString(
                List.of("Sets the players label shown in dungeon browser lore"),
                file, fileConfiguration, "dungeonBrowserPlayersLabel", "&2Players:", true);
        dungeonBrowserPlayerEntryFormat = ConfigurationEngine.setString(
                List.of("Sets the format for each player entry in dungeon browser lore",
                        "$playerName is the placeholder for the player's display name"),
                file, fileConfiguration, "dungeonBrowserPlayerEntryFormat", "&f- $playerName", true);
        dungeonBrowserLevelLabel = ConfigurationEngine.setString(
                List.of("Sets the level label shown in dungeon browser lore",
                        "$level is the placeholder for the dungeon level"),
                file, fileConfiguration, "dungeonBrowserLevelLabel", "&7Level: &e$level", true);

        // WorldOperationQueue messages
        dungeonPreparingQueueMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player's dungeon preparation is queued",
                        "$position is the placeholder for the queue position"),
                file, fileConfiguration, "dungeonPreparingQueueMessage", "[EliteMobs] Preparing your dungeon... (Queue position: $position)", true);
        dungeonPreparingMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player's dungeon is being prepared"),
                file, fileConfiguration, "dungeonPreparingMessage", "[EliteMobs] Preparing your dungeon...", true);
        dungeonPrepareFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when dungeon preparation fails during the async phase"),
                file, fileConfiguration, "dungeonPrepareFailedMessage", "[EliteMobs] Failed to prepare dungeon. Please try again.", true);
        dungeonLoadFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when dungeon loading fails during the sync phase"),
                file, fileConfiguration, "dungeonLoadFailedMessage", "[EliteMobs] Failed to load dungeon. Please try again.", true);
        dungeonCancelledShutdownMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when dungeon preparation is cancelled due to server shutdown"),
                file, fileConfiguration, "dungeonCancelledShutdownMessage", "[EliteMobs] Dungeon preparation cancelled due to server shutdown.", true);

        // DungeonInstance messages
        dungeonDataFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when dungeon data cannot be found",
                        "$dungeon is the placeholder for the dungeon config name"),
                file, fileConfiguration, "dungeonDataFailedMessage", "[EliteMobs] Failed to get data for dungeon $dungeon! The dungeon will not start.", true);
        dungeonNoPermissionMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player doesn't have permission for a dungeon"),
                file, fileConfiguration, "dungeonNoPermissionMessage", "[EliteMobs] You don't have the permission to go to this dungeon!", true);
        dungeonCancelledEventMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when the instancing event is cancelled"),
                file, fileConfiguration, "dungeonCancelledEventMessage", "[EliteMobs] Something cancelled the instancing event! The dungeon will not start.", true);
        dungeonCopyFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when world copy fails"),
                file, fileConfiguration, "dungeonCopyFailedMessage", "[EliteMobs] Failed to copy the world! Report this to the dev. The dungeon will not start.", true);
        dungeonWorldLoadFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when world loading fails"),
                file, fileConfiguration, "dungeonWorldLoadFailedMessage", "[EliteMobs] Failed to load the world! Report this to the dev. The dungeon will not start.", true);
        dungeonDifficultyMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player joins a dungeon with level sync",
                        "$difficulty is the placeholder for the difficulty name",
                        "$levelSync is the placeholder for the level sync cap"),
                file, fileConfiguration, "dungeonDifficultyMessage", "[EliteMobs] Dungeon difficulty is set to $difficulty! Level sync caps your item level to $levelSync.", true);

        // DynamicDungeonInstance messages
        dynamicDungeonDataFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when dynamic dungeon data cannot be found",
                        "$dungeon is the placeholder for the dungeon config name"),
                file, fileConfiguration, "dynamicDungeonDataFailedMessage", "[EliteMobs] Failed to get data for dynamic dungeon $dungeon! The dungeon will not start.", true);
        dynamicDungeonNoPermissionMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player doesn't have permission for a dynamic dungeon"),
                file, fileConfiguration, "dynamicDungeonNoPermissionMessage", "[EliteMobs] You don't have the permission to go to this dungeon!", true);
        dynamicDungeonWorldLoadFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when world loading fails for a dynamic dungeon"),
                file, fileConfiguration, "dynamicDungeonWorldLoadFailedMessage", "[EliteMobs] Failed to load the world! Report this to the dev. The dungeon will not start.", true);
        dynamicDungeonLevelSetMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player joins a dynamic dungeon",
                        "$level is the placeholder for the selected level"),
                file, fileConfiguration, "dynamicDungeonLevelSetMessage", "[EliteMobs] Dynamic dungeon level set to $level!", true);

        // EnchantmentDungeonInstance messages
        enchantNoChallengeMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when no challenge dungeons are installed"),
                file, fileConfiguration, "enchantNoChallengeMessage", "&8[EliteMobs] &cYou rolled challenge but your server has not installed any challenge dungeons! &2This will count as an automatic enchantment success.", true);
        enchantChallengeCompleteMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when an enchantment challenge is completed"),
                file, fileConfiguration, "enchantChallengeCompleteMessage", "&8[EliteMobs] &2Challenge complete! Instance will close in 10 seconds. &6You can leave earlier by doing &9/em quit&6!", true);
        enchantChallengeSuccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when an enchantment challenge succeeds"),
                file, fileConfiguration, "enchantChallengeSuccessMessage", "&8[EliteMobs] &2You succeeded your item enchantment challenge! Your item has been successfully enchanted.", true);
        enchantCriticalFailureMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown on critical enchantment failure",
                        "$item is the placeholder for the item display name"),
                file, fileConfiguration, "enchantCriticalFailureMessage", "&8[EliteMobs] &4Critical enchantment failure! You have lost $item!", true);
        enchantChallengeFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when an enchantment challenge fails",
                        "$item is the placeholder for the item display name"),
                file, fileConfiguration, "enchantChallengeFailedMessage", "&8[EliteMobs] &cYou have failed the enchantment challenge! Your item $item was not enchanted.", true);

        // WorldPackage messages
        contentInstalledMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when content is successfully installed",
                        "$name is the placeholder for the content name"),
                file, fileConfiguration, "contentInstalledMessage", "[EliteMobs] Successfully installed $name! To uninstall, do /em setup again and click on this content again.", true);
        contentUninstallFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when content uninstall fails",
                        "$name is the placeholder for the content name"),
                file, fileConfiguration, "contentUninstallFailedMessage", "&c[EliteMobs] Failed to uninstall $name! There may still be players in the world.", true);
        contentUninstalledMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when content is successfully uninstalled",
                        "$name is the placeholder for the content name"),
                file, fileConfiguration, "contentUninstalledMessage", "&a[EliteMobs] Successfully uninstalled $name!", true);

        // DynamicDungeonPackage messages
        dynamicDungeonAccessMessage1 = ConfigurationEngine.setString(
                List.of("Sets the first access message shown when a dynamic dungeon is installed"),
                file, fileConfiguration, "dynamicDungeonAccessMessage1", "&6Dynamic dungeons must be accessed either through the &a/em &6menu or an NPC! NPCs for premade EliteMobs content can be found at the Adventurer's Guild Hub map.", true);
        dynamicDungeonAccessMessage2 = ConfigurationEngine.setString(
                List.of("Sets the second access message shown when a dynamic dungeon is installed"),
                file, fileConfiguration, "dynamicDungeonAccessMessage2", "Dynamic dungeons allow you to select the level before entering based on your guild rank!", true);
        dynamicDungeonAccessMessage3 = ConfigurationEngine.setString(
                List.of("Sets the third access message shown when a dynamic dungeon is installed"),
                file, fileConfiguration, "dynamicDungeonAccessMessage3", "Remember that instanced dungeons create a world when you join them and remove that world when you are done playing in them!", true);

        // Setup menu tooltips (EMPackage)
        contentInstalledLine1 = ConfigurationEngine.setString(
                List.of("Sets the first line of the tooltip for installed content in the setup menu."),
                file, fileConfiguration, "contentInstalledLine1", "Content is installed!", true);
        contentInstalledLine2 = ConfigurationEngine.setString(
                List.of("Sets the second line of the tooltip for installed content in the setup menu."),
                file, fileConfiguration, "contentInstalledLine2", "Click to uninstall!", true);
        contentNotInstalledLine1 = ConfigurationEngine.setString(
                List.of("Sets the first line of the tooltip for content that is downloaded but not installed."),
                file, fileConfiguration, "contentNotInstalledLine1", "Content is not installed!", true);
        contentNotInstalledLine2 = ConfigurationEngine.setString(
                List.of("Sets the second line of the tooltip for content that is downloaded but not installed."),
                file, fileConfiguration, "contentNotInstalledLine2", "Click to install!", true);
        contentPartialLine1 = ConfigurationEngine.setString(
                List.of("Sets the first line of the tooltip for partially installed content."),
                file, fileConfiguration, "contentPartialLine1", "Content partially installed!", true);
        contentPartialLine2 = ConfigurationEngine.setString(
                List.of("Sets the second line of the tooltip for partially installed content."),
                file, fileConfiguration, "contentPartialLine2", "This is either because you haven't downloaded all of it,", true);
        contentPartialLine3 = ConfigurationEngine.setString(
                List.of("Sets the third line of the tooltip for partially installed content."),
                file, fileConfiguration, "contentPartialLine3", "or because some elements have been manually disabled.", true);
        contentPartialLine4 = ConfigurationEngine.setString(
                List.of("Sets the fourth line of the tooltip for partially installed content."),
                file, fileConfiguration, "contentPartialLine4", "Click to download!", true);
        contentNotDownloadedLine1 = ConfigurationEngine.setString(
                List.of("Sets the first line of the tooltip for content that has not been downloaded."),
                file, fileConfiguration, "contentNotDownloadedLine1", "Content is not downloaded!", true);
        contentNotDownloadedLine2 = ConfigurationEngine.setString(
                List.of("Sets the second line of the tooltip for content that has not been downloaded."),
                file, fileConfiguration, "contentNotDownloadedLine2", "Click to download!", true);
        contentNeedAccessMessage = ConfigurationEngine.setString(
                List.of("Sets the tooltip message shown when access is required to download content."),
                file, fileConfiguration, "contentNeedAccessMessage", "&cYou need access to download this content!", true);
        contentAccessClickMessage = ConfigurationEngine.setString(
                List.of("Sets the tooltip message prompting the user to click for access info."),
                file, fileConfiguration, "contentAccessClickMessage", "&eClick to see how to get access.", true);
        contentAvailablePatreon = ConfigurationEngine.setString(
                List.of("Sets the tooltip message indicating content is available via Patreon."),
                file, fileConfiguration, "contentAvailablePatreon", "&7Available via: &6Patreon", true);
        contentAvailableItch = ConfigurationEngine.setString(
                List.of("Sets the tooltip message indicating content is available via itch.io."),
                file, fileConfiguration, "contentAvailableItch", "&7Available via: &ditch.io", true);
        contentUpdateAvailable = ConfigurationEngine.setString(
                List.of("Sets the tooltip message indicating an update is available."),
                file, fileConfiguration, "contentUpdateAvailable", "&eUpdate available!", true);
        contentUpdateClickMessage = ConfigurationEngine.setString(
                List.of("Sets the tooltip message prompting the user to click to update."),
                file, fileConfiguration, "contentUpdateClickMessage", "&aClick to update automatically!", true);
        contentUpdateNeedAccess = ConfigurationEngine.setString(
                List.of("Sets the tooltip message indicating access is required for the update."),
                file, fileConfiguration, "contentUpdateNeedAccess", "&cYou need access to download this update.", true);
        contentUpdateAccessClick = ConfigurationEngine.setString(
                List.of("Sets the tooltip message prompting the user to click for access info for updates."),
                file, fileConfiguration, "contentUpdateAccessClick", "&7Click to see how to get access.", true);

        // Download messages (EMPackage)
        contentDownloadLegacyMessage = ConfigurationEngine.setString(
                List.of("Sets the legacy download message shown when no Nightbreak slug is configured.",
                        "$link is the placeholder for the download link."),
                file, fileConfiguration, "contentDownloadLegacyMessage", "&4Download this at &9$link &4!", true);
        contentDownloadSeparator = ConfigurationEngine.setString(
                List.of("Sets the separator line used in download messages."),
                file, fileConfiguration, "contentDownloadSeparator", "----------------------------------------------------", true);
        contentNightbreakPromptLine1 = ConfigurationEngine.setString(
                List.of("Sets the first line of the Nightbreak account prompt."),
                file, fileConfiguration, "contentNightbreakPromptLine1", "&eThis content can be downloaded automatically with a Nightbreak account.", true);
        contentNightbreakPromptLine2 = ConfigurationEngine.setString(
                List.of("Sets the second line of the Nightbreak account prompt."),
                file, fileConfiguration, "contentNightbreakPromptLine2", "&71. Get your token at: &9https://nightbreak.io/account", true);
        contentNightbreakPromptLine3 = ConfigurationEngine.setString(
                List.of("Sets the third line of the Nightbreak account prompt."),
                file, fileConfiguration, "contentNightbreakPromptLine3", "&72. Run: &e/nightbreaklogin <your-token>", true);
        contentNightbreakPromptLine4 = ConfigurationEngine.setString(
                List.of("Sets the fourth line of the Nightbreak account prompt."),
                file, fileConfiguration, "contentNightbreakPromptLine4", "&7Or download manually at: &9https://nightbreak.io/plugin/elitemobs", true);
        contentCheckingAccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when checking access for content.",
                        "$name is the placeholder for the content name."),
                file, fileConfiguration, "contentCheckingAccessMessage", "&7[EliteMobs] Checking access for $name...", true);
        contentAccessFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when access checking fails."),
                file, fileConfiguration, "contentAccessFailedMessage", "&c[EliteMobs] Failed to check access. Try again later or download manually:", true);
        contentAccessFailedLink = ConfigurationEngine.setString(
                List.of("Sets the link shown when access checking fails."),
                file, fileConfiguration, "contentAccessFailedLink", "&9https://nightbreak.io/plugin/elitemobs", true);
        contentDownloadedReloadMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when content has been downloaded and a reload is triggered."),
                file, fileConfiguration, "contentDownloadedReloadMessage", "&a[EliteMobs] Content downloaded! Reloading EliteMobs...", true);

        // Access info messages (EMPackage)
        contentNoAccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when the player does not have access to content.",
                        "$name is the placeholder for the content name."),
                file, fileConfiguration, "contentNoAccessMessage", "&cYou don't have access to: &f$name", true);
        contentGetAccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message prompting the player on how to get access."),
                file, fileConfiguration, "contentGetAccessMessage", "&eYou can get access through:", true);
        contentNightbreakLink = ConfigurationEngine.setString(
                List.of("Sets the Nightbreak link shown in the access info."),
                file, fileConfiguration, "contentNightbreakLink", "&a\u2022 Nightbreak: &9https://nightbreak.io/plugin/elitemobs", true);
        contentPatreonLink = ConfigurationEngine.setString(
                List.of("Sets the Patreon link shown in the access info.",
                        "$link is the placeholder for the Patreon link."),
                file, fileConfiguration, "contentPatreonLink", "&6\u2022 Patreon: &9$link", true);
        contentItchLink = ConfigurationEngine.setString(
                List.of("Sets the itch.io link shown in the access info.",
                        "$link is the placeholder for the itch.io link."),
                file, fileConfiguration, "contentItchLink", "&d\u2022 itch.io: &9$link", true);
        contentLinkAccountMessage = ConfigurationEngine.setString(
                List.of("Sets the message telling the player how to link their account after purchasing."),
                file, fileConfiguration, "contentLinkAccountMessage", "&7After purchasing, use &e/nightbreaklogin <token> &7to link your account.", true);

        // WorldInstancedDungeonPackage messages
        instancedDungeonInstalledMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when an instanced dungeon is installed.",
                        "$name is the placeholder for the dungeon filename."),
                file, fileConfiguration, "instancedDungeonInstalledMessage", "&2Dungeon $name installed!", true);
        instancedDungeonAccessMessage = ConfigurationEngine.setString(
                List.of("Sets the access message shown when an instanced dungeon is installed."),
                file, fileConfiguration, "instancedDungeonAccessMessage", "&6Instanced dungeons must be accessed either through the &a/em &6menu or an NPC! NPCs for premade EliteMobs content can be found at the Adventurer's Guild Hub map.", true);
        instancedDungeonInstallNote = ConfigurationEngine.setString(
                List.of("Sets the note reminding players that instanced dungeons create and remove worlds."),
                file, fileConfiguration, "instancedDungeonInstallNote", "Remember that instanced dungeons create a world when you join them and remove that world when you are done playing in them!", true);
        enchantmentDungeonInstalledMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when an enchantment instanced dungeon is installed."),
                file, fileConfiguration, "enchantmentDungeonInstalledMessage", "&8[EliteMobs] &2Enchantment instanced dungeon installed! This dungeon can only be accessed when attempting to enchant items and getting a challenge as a result!", true);

        // InstanceDeathLocation messages
        instancePunchToRez = ConfigurationEngine.setString(
                List.of("Sets the text shown above death banners instructing players to punch to resurrect."),
                file, fileConfiguration, "instancePunchToRez", "&2Punch to rez!", true);
        instanceLivesLeft = ConfigurationEngine.setString(
                List.of("Sets the text shown below death banners indicating remaining lives.",
                        "$amount is the placeholder for the number of lives remaining."),
                file, fileConfiguration, "instanceLivesLeft", "$amount lives left!", true);

        // SkillSystemMigration messages
        skillMigrationSeparator = ConfigurationEngine.setString(
                List.of("Sets the separator line used in the skill migration notification."),
                file, fileConfiguration, "skillMigrationSeparator", "&8&m------------------------------------------------", true);
        skillMigrationTitle = ConfigurationEngine.setString(
                List.of("Sets the title of the skill migration notification."),
                file, fileConfiguration, "skillMigrationTitle", "&5&lSKILL SYSTEM ACTIVATED", true);
        skillMigrationLine1 = ConfigurationEngine.setString(
                List.of("Sets the first line of the skill migration notification."),
                file, fileConfiguration, "skillMigrationLine1", "&7EliteMobs now uses a new skill leveling system!", true);
        skillMigrationLine2 = ConfigurationEngine.setString(
                List.of("Sets the second line of the skill migration notification."),
                file, fileConfiguration, "skillMigrationLine2", "&eYou now level individual skills by killing mobs:", true);
        skillMigrationLine3 = ConfigurationEngine.setString(
                List.of("Sets the third line of the skill migration notification."),
                file, fileConfiguration, "skillMigrationLine3", "&7- Swords, Axes, Bows, Crossbows, Tridents, Hoes", true);
        skillMigrationLine4 = ConfigurationEngine.setString(
                List.of("Sets the fourth line of the skill migration notification."),
                file, fileConfiguration, "skillMigrationLine4", "&7- Armor levels on every kill at 1/3 XP rate", true);
        skillMigrationLine5 = ConfigurationEngine.setString(
                List.of("Sets the fifth line of the skill migration notification."),
                file, fileConfiguration, "skillMigrationLine5", "&cAll players start fresh with Level 1 in all skills.", true);
        skillMigrationLine6 = ConfigurationEngine.setString(
                List.of("Sets the sixth line of the skill migration notification."),
                file, fileConfiguration, "skillMigrationLine6", "&7Type &e/em &7and click &5Skills &7to view your progress!", true);

        // DynamicDungeonPackage install message
        dynamicDungeonInstalledMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a dynamic dungeon is installed.",
                        "$name is the placeholder for the dungeon filename."),
                file, fileConfiguration, "dynamicDungeonInstalledMessage", "&2Dynamic dungeon $name installed!", true);

        // VorpalStrikeSkill messages
        vorpalStrikeReadyMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when Vorpal Strike comes off cooldown."),
                file, fileConfiguration, "vorpalStrikeReadyMessage", "§6Vorpal Strike §aready!", true);

        // SoulSiphonSkill messages
        soulSiphonDecayMessage = ConfigurationEngine.setString(
                List.of("Sets the action bar message shown when Soul Siphon stacks decay."),
                file, fileConfiguration, "soulSiphonDecayMessage", "§7Soul Siphon stacks decayed", true);

        // SpectralScytheSkill messages
        spectralScytheCooldownMessage = ConfigurationEngine.setString(
                List.of("Sets the action bar message shown when Spectral Scythe is on cooldown.",
                        "$time is the placeholder for the remaining cooldown in seconds."),
                file, fileConfiguration, "spectralScytheCooldownMessage", "§cSpectral Scythe on cooldown: $times", true);

        // SkillXPHandler messages
        skillLevelUpTitle = ConfigurationEngine.setString(
                List.of("Sets the title shown when a player levels up a skill."),
                file, fileConfiguration, "skillLevelUpTitle", "&6&lLEVEL UP!", true);
        skillLevelUpSubtitle = ConfigurationEngine.setString(
                List.of("Sets the subtitle shown when a player levels up a skill.",
                        "$skill is the placeholder for the skill display name.",
                        "$level is the placeholder for the new level."),
                file, fileConfiguration, "skillLevelUpSubtitle", "&e$skill &7→ &aLevel $level", true);
        skillLevelUpBroadcast = ConfigurationEngine.setString(
                List.of("Sets the server-wide broadcast message when a player levels up a skill.",
                        "$player is the placeholder for the player name.",
                        "$skill is the placeholder for the skill display name.",
                        "$level is the placeholder for the new level."),
                file, fileConfiguration, "skillLevelUpBroadcast", "&6$player &7reached &e$skill &7level &a$level&7!", true);
        skillXpNoGainMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player gains no XP because the mob is too low level.",
                        "$mobLevel is the placeholder for the mob's level.",
                        "$skill is the placeholder for the skill name.",
                        "$playerLevel is the placeholder for the player's combat level."),
                file, fileConfiguration, "skillXpNoGainMessage", "&7[&eEliteMobs&7] &cNo XP gained &7- this mob (level &f$mobLevel&7) is too far below your &e$skill &7level (&f$playerLevel&7). Fight stronger mobs!", true);
        skillXpCappedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when XP is capped because the mob is too high level.",
                        "$mobLevel is the placeholder for the mob's level.",
                        "$skill is the placeholder for the skill name.",
                        "$playerLevel is the placeholder for the player's combat level.",
                        "$xp is the placeholder for the capped level."),
                file, fileConfiguration, "skillXpCappedMessage", "&7[&eEliteMobs&7] &6XP capped &7- this mob (level &f$mobLevel&7) is below your &e$skill &7level (&f$playerLevel&7). XP reduced to &f$xp&7.", true);

        // SkillBonus messages
        skillActivationFormat = ConfigurationEngine.setString(
                List.of("Sets the action bar format shown when a skill activates.",
                        "$skillName is the placeholder for the skill name."),
                file, fileConfiguration, "skillActivationFormat", "&6&l$skillName!", true);
        skillStackFormat = ConfigurationEngine.setString(
                List.of("Sets the action bar format shown when a stacking skill gains stacks.",
                        "$skillName is the placeholder for the skill name.",
                        "$current is the placeholder for the current stack count.",
                        "$max is the placeholder for the maximum stack count."),
                file, fileConfiguration, "skillStackFormat", "&6&l$skillName &7($current/$max stacks)", true);

        // Taze messages
        tazeShockedTitle = ConfigurationEngine.setString(
                List.of("Title shown on screen when a player is hit by the Taze power.",
                        "Leave empty for subtitle only."),
                file, fileConfiguration, "tazeShockedTitle", "", true);
        tazeShockedSubtitle = ConfigurationEngine.setString(
                List.of("Sets the subtitle shown when a player is tazed by an elite mob."),
                file, fileConfiguration, "tazeShockedSubtitle", "Shocked!", true);

        // LegionsDisciplineSkill messages
        legionsDisciplineBrokenMessage = ConfigurationEngine.setString(
                List.of("Sets the action bar message shown when discipline stacks are broken."),
                file, fileConfiguration, "legionsDisciplineBrokenMessage", "&7Discipline broken...", true);

        // AvatarOfJudgmentSkill messages
        avatarFadesMessage = ConfigurationEngine.setString(
                List.of("Sets the action bar message shown when the Avatar of Judgment buff expires."),
                file, fileConfiguration, "avatarFadesMessage", "&7Avatar fades...", true);

        // ModelsPackage messages
        modelsAutoInstallMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player tries to manually install models."),
                file, fileConfiguration, "modelsAutoInstallMessage", "Models are installed automatically, you can't do this manually!", true);
        modelsCannotUninstallMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player tries to uninstall models via the menu."),
                file, fileConfiguration, "modelsCannotUninstallMessage", "Models can't currently be uninstalled via this menu! Delete the Models folder in EliteMobs if you want to remove them.", true);

        // ItemsPackage messages
        itemsInstallingMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when items are being installed.",
                        "$count is the placeholder for the number of items."),
                file, fileConfiguration, "itemsInstallingMessage", "&2Installing $count items...", true);
        itemsUninstallingMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when items are being uninstalled.",
                        "$count is the placeholder for the number of items."),
                file, fileConfiguration, "itemsUninstallingMessage", "&2Uninstalling $count items...", true);
        itemsSavingMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when item files are being saved.",
                        "$count is the placeholder for the number of items."),
                file, fileConfiguration, "itemsSavingMessage", "&2Saving $count item files...", true);
        itemsReloadingMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when EliteMobs is reloading to apply item changes."),
                file, fileConfiguration, "itemsReloadingMessage", "Reloading EliteMobs to apply item changes!", true);

        // EventsPackage messages
        eventsInstallingMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when events are being installed.",
                        "$count is the placeholder for the number of events."),
                file, fileConfiguration, "eventsInstallingMessage", "&2Installing $count events...", true);
        eventsUninstallingMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when events are being uninstalled.",
                        "$count is the placeholder for the number of events."),
                file, fileConfiguration, "eventsUninstallingMessage", "&2Uninstalling $count events...", true);
        eventsSavingMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when event files are being saved.",
                        "$count is the placeholder for the number of events."),
                file, fileConfiguration, "eventsSavingMessage", "&2Saving $count event files...", true);
        eventsReloadingMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when EliteMobs is reloading to apply event changes."),
                file, fileConfiguration, "eventsReloadingMessage", "Reloading EliteMobs to apply event changes!", true);
    }
}
