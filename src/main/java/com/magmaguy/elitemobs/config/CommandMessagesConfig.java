package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

public class CommandMessagesConfig extends ConfigurationFile {

    // Currency commands
    @Getter
    private static String payNiceTryMessage;
    @Getter
    private static String payPlayerNotOnlineMessage;
    @Getter
    private static String currencyPlayerNotValidMessage;
    @Getter
    private static String currencyAddedMessage;
    @Getter
    private static String currencyNowHasMessage;
    @Getter
    private static String currencyAddedAllMessage;
    @Getter
    private static String currencySubtractedMessage;
    @Getter
    private static String currencySetMessage;
    @Getter
    private static String currencyCheckMessage;

    // Dungeon commands
    @Getter
    private static String dungeonNotValidMessage;
    @Getter
    private static String dungeonNotInstalledMessage;
    @Getter
    private static String alreadyInInstanceMessage;
    @Getter
    private static String dungeonTeleportNotSetMessage;

    // Quest commands
    @Getter
    private static String questPlayerNotValidMessage;
    @Getter
    private static String questResetSuccessMessage;
    @Getter
    private static String questNameNotValidMessage;
    @Getter
    private static String questCompletedSuccessMessage;
    @Getter
    private static String noActiveQuestsMessage;
    @Getter
    private static String questNotFoundMessage;

    // Boss/spawn commands
    @Getter
    private static String noSafeSpawnMessage;
    @Getter
    private static String bossNotValidRegionalMessage;
    @Getter
    private static String leashRadiusFailedMessage;
    @Getter
    private static String invalidEntityTypeMessage;
    @Getter
    private static String filenameNotValidMessage;
    @Getter
    private static String worldNotValidMessage;
    @Getter
    private static String invalidPowerMessage;

    // Admin commands
    @Getter
    private static String killedElitesMessage;
    @Getter
    private static String killedEliteTypeMessage;
    @Getter
    private static String invalidEliteTypeMessage;
    @Getter
    private static String removeElitesOffMessage;
    @Getter
    private static String removeElitesOnMessage;
    @Getter
    private static String removedNotEliteMobsMessage;
    @Getter
    private static String removedHijackedMessage;
    @Getter
    private static String removedTreasureChestMessage;
    @Getter
    private static String removedSpawnLocationMessage;
    @Getter
    private static String invalidNpcFilenameMessage;
    @Getter
    private static String gaveItemMessage;
    @Getter
    private static String invalidItemFilenameMessage;
    @Getter
    private static String invalidPlayerForItemMessage;
    @Getter
    private static String eventQueuedMessage;
    @Getter
    private static String invalidEventMessage;
    @Getter
    private static String eventCancelledMessage;
    @Getter
    private static String eventNoSpawnTypeMessage;
    @Getter
    private static String eventInvalidSpawnTypeMessage;
    @Getter
    private static String eventNoValidBossesMessage;
    @Getter
    private static String invalidMinidungeonMessage;
    @Getter
    private static String minidungeonNotInstalledMessage;
    @Getter
    private static String invalidAnchorPointMessage;
    @Getter
    private static String packageDoneMessage;
    @Getter
    private static String packageDontForgetMessage;
    @Getter
    private static String packageZipFailedMessage;
    @Getter
    private static String packageZippedMessage;
    @Getter
    private static String packageNeedsDungeonNameMessage;
    @Getter
    private static String packageNeedsNumberMessage;
    @Getter
    private static String packageNoSubdirectoryMessage;
    @Getter
    private static String packageFailedDirectoryMessage;
    @Getter
    private static String missingPermissionMessage;
    @Getter
    private static String regionalBossStatsMessage;

    // Language command
    @Getter
    private static String languageNotFoundMessage;
    @Getter
    private static String languageListPrefix;
    @Getter
    private static String languageSetEnglishMessage;
    @Getter
    private static String languageGeneratingCustomMessage;
    @Getter
    private static String languageGenerateFailedMessage;
    @Getter
    private static String languageGenerateSuccessMessage;
    @Getter
    private static String languageSetCustomMessage;
    @Getter
    private static String languageDownloadingMessage;
    @Getter
    private static String languageDownloadFailedMessage;
    @Getter
    private static String languageDownloadSuccessMessage;
    @Getter
    private static String languageSetMessage;

    // Skill commands
    @Getter
    private static String skillPlayerNotFoundMessage;
    @Getter
    private static String skillCheckHeaderMessage;
    @Getter
    private static String skillCheckEntryFormat;
    @Getter
    private static String skillInvalidTypeMessage;
    @Getter
    private static String skillValidTypesMessage;
    @Getter
    private static String skillLevelMinMessage;
    @Getter
    private static String skillLevelWarningMessage;
    @Getter
    private static String skillSetSuccessMessage;
    @Getter
    private static String skillSetAllSuccessMessage;
    @Getter
    private static String skillTestOnlyPlayerMessage;
    @Getter
    private static String skillTestAlreadyRunningMessage;
    @Getter
    private static String skillTestStartMessage;
    @Getter
    private static String skillTestInfoMessage1;
    @Getter
    private static String skillTestInfoMessage2;
    @Getter
    private static String skillTestCancelNoTestMessage;
    @Getter
    private static String skillTestCancelSuccessMessage;

    // Debug command
    @Getter
    private static String debugEnabledMessage;
    @Getter
    private static String debugDisabledMessage;

    // Discord command
    @Getter
    private static String discordMessage;
    @Getter
    private static String discordMessageSentMessage;

    // Scroll commands
    @Getter
    private static String eliteScrollConfigMessage;
    @Getter
    private static String scrollsNotEnabledMessage;
    @Getter
    private static String scrollInvalidNumberMessage;
    @Getter
    private static String scrollLevelZeroMessage;
    @Getter
    private static String scrollAmountZeroMessage;
    @Getter
    private static String scrollGaveMessage;

    // Other commands
    @Getter
    private static String helpHeaderMessage;
    @Getter
    private static String wormholeInvalidOptionMessage;
    @Getter
    private static String wormholeSetFailedMessage;
    @Getter
    private static String protectionBypassMessage;
    @Getter
    private static String setupDoneDisableMessage;
    @Getter
    private static String setupDoneEnableMessage;
    @Getter
    private static String setupNotValidPackageMessage;
    @Getter
    private static String setupInstalledMessage;
    @Getter
    private static String setupUninstalledMessage;
    @Getter
    private static String shopPlayerNotFoundMessage;
    @Getter
    private static String notQueuedForInstanceMessage;
    @Getter
    private static String reloadStartMessage;
    @Getter
    private static String reloadSuccessMessage;
    @Getter
    private static String getTierGaveGearMessage;
    @Getter
    private static String getTierIronSwordMessage;
    @Getter
    private static String getTierCheatSwordMessage;

    // TransitiveBlockCommand messages
    @Getter
    private static String transitiveBlockStartMessage;
    @Getter
    private static String transitiveBlockInvalidBossMessage;
    @Getter
    private static String transitiveBlockNotRegionalMessage;
    @Getter
    private static String transitiveBlockIgnoreWarningMessage;
    @Getter
    private static String transitiveBlockInvalidTypeMessage;
    @Getter
    private static String transitiveBlockCorner1Message;
    @Getter
    private static String transitiveBlockCorner2Message;
    @Getter
    private static String transitiveBlockSelectionCountMessage;
    @Getter
    private static String transitiveBlockRegisteredMessage;
    @Getter
    private static String transitiveBlockRegisteredAirMessage;
    @Getter
    private static String transitiveBlockUnregisteredMessage;
    @Getter
    private static String transitiveBlockTooManyMessage;
    @Getter
    private static String transitiveBlockRequiresRegionalMessage;
    @Getter
    private static String transitiveBlockCancellingMessage;
    @Getter
    private static String transitiveBlockMultipleBossesMessage;
    @Getter
    private static String transitiveBlockMultipleBossesHintMessage;
    @Getter
    private static String transitiveBlockNowRegisteringMessage;
    @Getter
    private static String transitiveBlockRunSameCommandMessage;
    @Getter
    private static String transitiveBlockCancelCommandMessage;
    @Getter
    private static String transitiveBlockCancelledMessage;
    @Getter
    private static String transitiveBlockNowSavingMessage;
    @Getter
    private static String transitiveBlockRegisteredCornerMessage;
    @Getter
    private static String transitiveBlockLocationsRegisteredMessage;
    @Getter
    private static String transitiveBlockLocationsFailedMessage;

    // LootStats messages
    @Getter
    private static String lootStatsHeader;
    @Getter
    private static String lootStatsAttackSpeed;
    @Getter
    private static String lootStatsDamage;
    @Getter
    private static String lootStatsEdps;
    @Getter
    private static String lootStatsLevel;
    @Getter
    private static String lootStatsBonusEdps;

    // SimLootCommand messages
    @Getter
    private static String simLootPlayerNotFound;
    @Getter
    private static String simLootFinishedMultiple;
    @Getter
    private static String simLootFinishedSingle;
    @Getter
    private static String simLootCouldNotSend;

    // StatsCommand extra messages
    @Getter
    private static String trackedBossCountMessage;
    @Getter
    private static String trackedNpcCountMessage;
    @Getter
    private static String statsSeparator;
    @Getter
    private static String statsVersionHeader;
    @Getter
    private static String statsEntityCountMessage;
    @Getter
    private static String statsBreakdownPrefix;
    @Getter
    private static String statsHighestThreatMessage;
    @Getter
    private static String statsAverageThreatMessage;

    // NPCInteractions messages
    @Getter
    private static String npcFeatureComingSoonMessage;
    @Getter
    private static String npcCannotRenameMessage;
    @Getter
    private static String npcFeatureReplacedMessage;
    @Getter
    private static String npcRemoveInstructions;

    // SharedLootTable messages
    @Getter
    private static String lootVoteSeparator;
    @Getter
    private static String lootVoteMessage;

    // VersionChecker messages
    @Getter
    private static String versionCheckConnectionWarning;
    @Getter
    private static String versionOutdatedMessage;
    @Getter
    private static String resourcePackUpdatedMessage;
    @Getter
    private static String contentUpdatesAvailable;
    @Getter
    private static String contentUpdateEntry;
    @Getter
    private static String dungeonsOutdatedMessage;
    @Getter
    private static String versionUseSetupMessage;
    @Getter
    private static String versionClickDownloadHover;
    @Getter
    private static String versionNoDownloadLink;
    @Getter
    private static String versionDownloadAtMessage;
    @Getter
    private static String versionUpdateEasyMessage;
    @Getter
    private static String versionClickHereLabel;
    @Getter
    private static String versionInstallInfoMessage;
    @Getter
    private static String versionHereLabel;
    @Getter
    private static String versionSupportRoomMessage;
    @Getter
    private static String versionAutoUpdateMessage;
    @Getter
    private static String versionAutoUpdateHover;
    @Getter
    private static String versionOutdatedEntryPrefix;
    @Getter
    private static String versionNightbreakHover;
    @Getter
    private static String versionWikiHover;
    @Getter
    private static String versionDiscordHover;

    // WormholeEntry OP message
    @Getter
    private static String wormholeOpDownloadMessage;

    // FarmingProtection messages
    @Getter
    private static String farmingWarningMessage;
    @Getter
    private static String farmingResetMessage;
    @Getter
    private static String farmingCapMessage;

    // SharedLootTable need/greed messages
    @Getter
    private static String lootNeedMessage;
    @Getter
    private static String lootGreedMessage;

    // QuestCommand messages
    @Getter
    private static String questInvalidIdMessage;

    // QuestBypassCommand messages
    @Getter
    private static String questBypassOnMessage;
    @Getter
    private static String questBypassOffMessage;

    // CustomTreasureChestsConfig messages
    @Getter
    private static String invalidTreasureChestFilename;

    // RelativeCoordinatesCommand messages
    @Getter
    private static String relativePositionMessage;

    // GetTierCommand display names
    @Getter
    private static String cheatSwordDisplayName;

    // Download all content command
    @Getter
    private static String downloadAllNoTokenMessage;
    @Getter
    private static String downloadAllNothingMessage;
    @Getter
    private static String downloadAllNoAccessMessage;
    @Getter
    private static String downloadAllFoundMessage;
    @Getter
    private static String downloadAllProgressMessage;
    @Getter
    private static String downloadAllDownloadedMoreMessage;
    @Getter
    private static String downloadAllDownloadedMessage;
    @Getter
    private static String downloadAllFailedMessage;
    @Getter
    private static String downloadAllCompleteMessage;
    @Getter
    private static String downloadAllReloadingMessage;
    @Getter
    private static String downloadAllSetupClickMessage;
    @Getter
    private static String downloadAllSetupClickHover;

    // Update content command
    @Getter
    private static String updateNoTokenMessage;
    @Getter
    private static String updateAllUpToDateMessage;
    @Getter
    private static String updateFoundOutdatedMessage;
    @Getter
    private static String updateCompleteMessage;
    @Getter
    private static String updateReloadingMessage;
    @Getter
    private static String updateProgressMessage;
    @Getter
    private static String updateDownloadedMoreMessage;
    @Getter
    private static String updateDownloadedMessage;
    @Getter
    private static String updateFailedMessage;

    public CommandMessagesConfig() {
        super("command_messages.yml");
    }

    @Override
    public void initializeValues() {
        // Currency commands
        payNiceTryMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player tries to pay a negative amount"),
                file, fileConfiguration, "payNiceTryMessage", "&4[EliteMobs]Nice try.", true);
        payPlayerNotOnlineMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player tries to pay an offline player", "$player - the target player name"),
                file, fileConfiguration, "payPlayerNotOnlineMessage", "&8[EliteMobs] &4Player $player is not online and can therefore not get a payment.", true);
        currencyPlayerNotValidMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player name is not valid for currency commands", "$player - the target player name"),
                file, fileConfiguration, "currencyPlayerNotValidMessage", "&8[EliteMobs] &4Player $player is not valid!", true);
        currencyAddedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when currency is added to a player", "$amount - the amount added", "$player - the target player name"),
                file, fileConfiguration, "currencyAddedMessage", "&8[EliteMobs] &2You have added $amount to $player", true);
        currencyNowHasMessage = ConfigurationEngine.setString(
                List.of("Sets the message showing a player's new balance after currency change", "$amount - the new balance"),
                file, fileConfiguration, "currencyNowHasMessage", "&8[EliteMobs] &2They now have $amount", true);
        currencyAddedAllMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when currency is added to all online players", "$amount - the amount added"),
                file, fileConfiguration, "currencyAddedAllMessage", "&8[EliteMobs] &2You have added $amount to all online players.", true);
        currencySubtractedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when currency is subtracted from a player", "$amount - the amount subtracted", "$player - the target player name"),
                file, fileConfiguration, "currencySubtractedMessage", "&8[EliteMobs] &2You have subtracted $amount from $player", true);
        currencySetMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player's currency is set", "$player - the target player name", "$currencyName - the currency name", "$amount - the amount set"),
                file, fileConfiguration, "currencySetMessage", "You set $player's $currencyName to $amount", true);
        currencyCheckMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when checking a player's currency", "$player - the target player name", "$amount - the balance", "$currencyName - the currency name"),
                file, fileConfiguration, "currencyCheckMessage", "&8[EliteMobs]&f $player &2has $amount $currencyName", true);

        // Dungeon commands
        dungeonNotValidMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a dungeon name is not valid"),
                file, fileConfiguration, "dungeonNotValidMessage", "[EliteMobs] That dungeon isn't valid!", true);
        dungeonNotInstalledMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a dungeon is not installed"),
                file, fileConfiguration, "dungeonNotInstalledMessage", "[EliteMobs] That dungeon isn't installed, ask an admin to install it!", true);
        alreadyInInstanceMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player is already in an instance"),
                file, fileConfiguration, "alreadyInInstanceMessage", "[EliteMobs] You're already in an instance! You will not be able to switch to another instance before you do /em quit", true);
        dungeonTeleportNotSetMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a dungeon teleport location is not set"),
                file, fileConfiguration, "dungeonTeleportNotSetMessage", "[EliteMobs] Can't teleport you to the dungeon because the teleport location isn't set! If you are an admin, check the DungeonPackager config for this content and manually set the teleport location.", true);

        // Quest commands
        questPlayerNotValidMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player name is not valid for quest commands"),
                file, fileConfiguration, "questPlayerNotValidMessage", "[EliteMobs] Error - player name not valid!", true);
        questResetSuccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when quests are successfully reset", "$player - the target player name"),
                file, fileConfiguration, "questResetSuccessMessage", "[EliteMobs] Successfully reset quests for player $player", true);
        questNameNotValidMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a quest filename is not valid", "$quest - the quest filename"),
                file, fileConfiguration, "questNameNotValidMessage", "[EliteMobs] Error - quest filename $quest is not valid!", true);
        questCompletedSuccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a quest is successfully completed for a player", "$quest - the quest name", "$player - the target player name"),
                file, fileConfiguration, "questCompletedSuccessMessage", "[EliteMobs] Successfully quest $quest for player $player", true);
        noActiveQuestsMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player has no active quests"),
                file, fileConfiguration, "noActiveQuestsMessage", "You have no active quests.", true);
        questNotFoundMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a quest is not found"),
                file, fileConfiguration, "questNotFoundMessage", "Quest not found.", true);

        // Boss/spawn commands
        noSafeSpawnMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when no safe spawn location is found"),
                file, fileConfiguration, "noSafeSpawnMessage", "[EliteMobs] No safe spawn location found! Make sure the area is passable!", true);
        bossNotValidRegionalMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a custom boss is not a valid regional boss", "$boss - the boss filename"),
                file, fileConfiguration, "bossNotValidRegionalMessage", "&8[EliteMobs] &4Failed to add spawn location! Custom Boss $boss is not valid regional boss!", true);
        leashRadiusFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when setting a leash radius fails"),
                file, fileConfiguration, "leashRadiusFailedMessage", "&8[EliteMobs] &4Failed set the leash radius! Was the boss a valid regional boss?", true);
        invalidEntityTypeMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an entity type cannot be an Elite", "$type - the entity type"),
                file, fileConfiguration, "invalidEntityTypeMessage", "&8[EliteMobs] &4Entity type $type can't be an Elite!", true);
        filenameNotValidMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a boss filename is not valid", "$filename - the filename"),
                file, fileConfiguration, "filenameNotValidMessage", "Filename $filename is not valid! Make sure you are writing the name of a configuration file in the custombosses folder!", true);
        worldNotValidMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a world argument is not valid"),
                file, fileConfiguration, "worldNotValidMessage", "[EliteMobs] World argument was not valid!", true);
        invalidPowerMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a power name is not valid", "$power - the power name"),
                file, fileConfiguration, "invalidPowerMessage", "[EliteMobs] Power $power is not a valid power! Valid powers:", true);

        // Admin commands
        killedElitesMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when elites are killed", "$count - the number killed"),
                file, fileConfiguration, "killedElitesMessage", "&8[EliteMobs] &4Killed $count Elite Mobs.", true);
        killedEliteTypeMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when elites of a specific type are killed", "$count - the number killed", "$type - the entity type"),
                file, fileConfiguration, "killedEliteTypeMessage", "&8[EliteMobs] &4Killed $count Elite $type.", true);
        invalidEliteTypeMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an entity type is not valid for EliteMobs"),
                file, fileConfiguration, "invalidEliteTypeMessage", "&8[EliteMobs] &cNot a valid entity type for EliteMobs!", true);
        removeElitesOffMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player stops removing elites"),
                file, fileConfiguration, "removeElitesOffMessage", "&8[EliteMobs] &aYou are no longer removing elites!", true);
        removeElitesOnMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player starts removing elites"),
                file, fileConfiguration, "removeElitesOnMessage", "&8[EliteMobs] &cYou are now removing elites when you punch them! Run &a/em remove &cagain to stop removing elites!", true);
        removedNotEliteMobsMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a removed entity was not an EliteMobs entity"),
                file, fileConfiguration, "removedNotEliteMobsMessage", "&8[EliteMobs] The entity you just removed was not an EliteMobs entity. EliteMobs will still attempt to remove it though.", true);
        removedHijackedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a removed entity was likely hijacked by another plugin"),
                file, fileConfiguration, "removedHijackedMessage", "&8[EliteMobs] If the entity is supposed to be an EliteMobs entity, it is highly likely some other plugin hijacked the entity and changed it in a way that made EliteMobs unable to recognize it anymore.", true);
        removedTreasureChestMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a treasure chest is removed"),
                file, fileConfiguration, "removedTreasureChestMessage", "[EliteMobs] Removed treasure chest!", true);
        removedSpawnLocationMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a regional boss spawn location is removed", "$boss - the boss filename"),
                file, fileConfiguration, "removedSpawnLocationMessage", "&8[EliteMobs] &cRemoved a spawn location for boss $boss", true);
        invalidNpcFilenameMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an NPC filename is invalid"),
                file, fileConfiguration, "invalidNpcFilenameMessage", "[EliteMobs] Invalid NPC filename.", true);
        gaveItemMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an item is successfully given to a player", "$player - the target player name", "$item - the item name"),
                file, fileConfiguration, "gaveItemMessage", "[EliteMobs] Successfully gave $player item $item", true);
        invalidItemFilenameMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an item filename is not valid", "$filename - the filename"),
                file, fileConfiguration, "invalidItemFilenameMessage", "&8[EliteMobs] &cFile name $filename is not a valid custom item file name!", true);
        invalidPlayerForItemMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when trying to give an item to an invalid player"),
                file, fileConfiguration, "invalidPlayerForItemMessage", "&8[EliteMobs] &cTried to give item to invalid player!", true);
        eventQueuedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an event is queued", "$event - the event name"),
                file, fileConfiguration, "eventQueuedMessage", "&8[EliteMobs] Queued event $event for the next valid time it can spawn. &cThis might take a while depending on the start conditions of the event.", true);
        invalidEventMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an event name is not valid", "$event - the event name"),
                file, fileConfiguration, "invalidEventMessage", "&8[EliteMobs] &4Event $event is not valid! Make sure the event file exists, is enabled and has eventType set to TIMED.", true);
        eventCancelledMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an event is cancelled by another plugin", "$event - the event name"),
                file, fileConfiguration, "eventCancelledMessage", "&8[EliteMobs] &4Event $event was cancelled by another plugin!", true);
        eventNoSpawnTypeMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an event has no spawnType set", "$event - the event name"),
                file, fileConfiguration, "eventNoSpawnTypeMessage", "&8[EliteMobs] &4Event $event has no spawnType set! Add a valid custom spawn filename to the event config.", true);
        eventInvalidSpawnTypeMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an event's spawnType is not valid", "$event - the event name", "$spawnType - the invalid spawn type"),
                file, fileConfiguration, "eventInvalidSpawnTypeMessage", "&8[EliteMobs] &4Event $event failed to start because its spawnType '$spawnType' is not a valid custom spawn file! Make sure the file exists in the customspawns folder and is enabled.", true);
        eventNoValidBossesMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when none of an event's boss filenames are valid", "$event - the event name"),
                file, fileConfiguration, "eventNoValidBossesMessage", "&8[EliteMobs] &4Event $event failed to start because none of its boss filenames are valid! Check that the bossFilenames in the event config point to existing custom boss files.", true);
        invalidMinidungeonMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a minidungeon name is not valid", "$name - the minidungeon name"),
                file, fileConfiguration, "invalidMinidungeonMessage", "&8[EliteMobs] &4Minidungeons name $name isn't valid!", true);
        minidungeonNotInstalledMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a minidungeon is not installed"),
                file, fileConfiguration, "minidungeonNotInstalledMessage", "&8[EliteMobs] &4Minidungeon isn't installed! Can't get the relative location for uninstalled Minidungeons!", true);
        invalidAnchorPointMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an anchor point is not valid"),
                file, fileConfiguration, "invalidAnchorPointMessage", "&8[EliteMobs] &4Something went wrong and made the anchor point not valid!", true);
        packageDoneMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when packaging is done"),
                file, fileConfiguration, "packageDoneMessage", "&8[EliteMobs] &2Done! You can find your package in &9~/plugins/EliteMobs/exports &2. &6If you are making a dungeon, make sure to create your own dungeonpackages file!", true);
        packageDontForgetMessage = ConfigurationEngine.setString(
                List.of("Sets the reminder message about adding world and schematic files"),
                file, fileConfiguration, "packageDontForgetMessage", "&8[EliteMobs] &6Don't forget to add your world and schematic files to the package if needed!", true);
        packageZipFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when zipping a package fails"),
                file, fileConfiguration, "packageZipFailedMessage", "&4[EliteMobs] Failed to zip package!", true);
        packageZippedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a package is zipped successfully"),
                file, fileConfiguration, "packageZippedMessage", "&8[EliteMobs] &6Zipped files for your convenience. Don't forget any additional files like the dungeonpackager or world/schematics files before distributing!", true);
        packageNeedsDungeonNameMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a dungeon name is needed"),
                file, fileConfiguration, "packageNeedsDungeonNameMessage", "[EliteMobs] This commands needs a valid dungeon name!", true);
        packageNeedsNumberMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a valid number is needed"),
                file, fileConfiguration, "packageNeedsNumberMessage", "This command needs a valid natural number! (1, 2, 3, 4...)", true);
        packageNoSubdirectoryMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a subdirectory is not found", "$subdirectory - the subdirectory name"),
                file, fileConfiguration, "packageNoSubdirectoryMessage", "[EliteMobs] Could not find any $subdirectory for this dungeon. This might be normal depending on your setup.", true);
        packageFailedDirectoryMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a directory fails to be created", "$path - the directory path"),
                file, fileConfiguration, "packageFailedDirectoryMessage", "[EliteMobs] Failed to create directory $path", true);
        missingPermissionMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player is missing a permission"),
                file, fileConfiguration, "missingPermissionMessage", "Missing permission: elitemobs.adventurersguild.command / elitemobs.adventurersguild.teleport", true);
        regionalBossStatsMessage = ConfigurationEngine.setString(
                List.of("Sets the message showing regional boss stats", "$total - total regional bosses", "$loaded - currently loaded bosses"),
                file, fileConfiguration, "regionalBossStatsMessage", "&7[EM] &2There are &c$total &2Regional Bosses installed, of which &c$loaded &2are currently loaded.", true);

        // Language command
        languageNotFoundMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a language is not found"),
                file, fileConfiguration, "languageNotFoundMessage", "&cLanguage not found. Valid options:", true);
        languageListPrefix = ConfigurationEngine.setString(
                List.of("Sets the prefix for each language listed"),
                file, fileConfiguration, "languageListPrefix", "  - ", true);
        languageSetEnglishMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when language is set to English"),
                file, fileConfiguration, "languageSetEnglishMessage", "&2Language set to English! Using plugin defaults.", true);
        languageGeneratingCustomMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when generating custom.csv template"),
                file, fileConfiguration, "languageGeneratingCustomMessage", "&eGenerating custom.csv template...", true);
        languageGenerateFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when custom.csv generation fails"),
                file, fileConfiguration, "languageGenerateFailedMessage", "&cFailed to generate custom.csv. Language not changed.", true);
        languageGenerateSuccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when custom.csv is generated successfully"),
                file, fileConfiguration, "languageGenerateSuccessMessage", "&2Generated custom.csv! Edit it to customize text or add translations.", true);
        languageSetCustomMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when language is set to custom"),
                file, fileConfiguration, "languageSetCustomMessage", "&2Language set to custom! &eEdit plugins/EliteMobs/translations/custom.csv to customize text. New keys will be added automatically as the plugin runs.", true);
        languageDownloadingMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when downloading a language file", "$language - the language name"),
                file, fileConfiguration, "languageDownloadingMessage", "&eDownloading $language.csv...", true);
        languageDownloadFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a language download fails", "$language - the language name"),
                file, fileConfiguration, "languageDownloadFailedMessage", "&cFailed to download $language.csv. Language not changed.", true);
        languageDownloadSuccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a language file is downloaded successfully", "$language - the language name"),
                file, fileConfiguration, "languageDownloadSuccessMessage", "&2Downloaded $language.csv successfully.", true);
        languageSetMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when language is set to a community language", "$language - the language name"),
                file, fileConfiguration, "languageSetMessage", "&2Language set to $language! &eTranslations are community-managed. Use at your own discretion.", true);

        // Skill commands
        skillPlayerNotFoundMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player is not found for skill commands", "$player - the player name"),
                file, fileConfiguration, "skillPlayerNotFoundMessage", "&cPlayer not found: $player", true);
        skillCheckHeaderMessage = ConfigurationEngine.setString(
                List.of("Sets the header message for skill check", "$player - the player name"),
                file, fileConfiguration, "skillCheckHeaderMessage", "&6=== $player's Skills ===", true);
        skillCheckEntryFormat = ConfigurationEngine.setString(
                List.of("Sets the format for each skill entry in skill check",
                        "$skill - the skill name", "$level - the skill level",
                        "$xpProgress - current XP progress", "$xpNeeded - XP needed for next level",
                        "$progress - percentage progress"),
                file, fileConfiguration, "skillCheckEntryFormat", "&7$skill: &aLevel $level &7($xpProgress/$xpNeeded XP, $progress%)", true);
        skillInvalidTypeMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an invalid skill type is specified", "$type - the invalid type"),
                file, fileConfiguration, "skillInvalidTypeMessage", "&cInvalid skill type: $type", true);
        skillValidTypesMessage = ConfigurationEngine.setString(
                List.of("Sets the message listing valid skill types", "$types - the valid types"),
                file, fileConfiguration, "skillValidTypesMessage", "&7Valid types: $types", true);
        skillLevelMinMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a skill level is below minimum"),
                file, fileConfiguration, "skillLevelMinMessage", "&cLevel must be at least 1!", true);
        skillLevelWarningMessage = ConfigurationEngine.setString(
                List.of("Sets the warning message for levels above soft cap", "$level - the level"),
                file, fileConfiguration, "skillLevelWarningMessage", "&eWarning: Level $level is above the soft cap of 100.", true);
        skillSetSuccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a skill is set successfully",
                        "$player - the player name", "$skill - the skill name",
                        "$level - the level", "$xp - the XP amount"),
                file, fileConfiguration, "skillSetSuccessMessage", "&aSet $player's $skill skill to level $level ($xp XP)", true);
        skillSetAllSuccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when all skills are set successfully",
                        "$player - the player name", "$level - the level", "$xp - the XP amount"),
                file, fileConfiguration, "skillSetAllSuccessMessage", "&aSet all of $player's skills to level $level ($xp XP)", true);
        skillTestOnlyPlayerMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a non-player tries to run a skill test"),
                file, fileConfiguration, "skillTestOnlyPlayerMessage", "&cThis command can only be run by a player!", true);
        skillTestAlreadyRunningMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a skill test is already running"),
                file, fileConfiguration, "skillTestAlreadyRunningMessage", "&cA test is already running! Use &e/em skill test cancel &cto stop it.", true);
        skillTestStartMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a skill test starts"),
                file, fileConfiguration, "skillTestStartMessage", "&a[Test] Starting skill system test...", true);
        skillTestInfoMessage1 = ConfigurationEngine.setString(
                List.of("Sets the first info message for skill test"),
                file, fileConfiguration, "skillTestInfoMessage1", "&7This will temporarily modify your skill levels and selections.", true);
        skillTestInfoMessage2 = ConfigurationEngine.setString(
                List.of("Sets the second info message for skill test"),
                file, fileConfiguration, "skillTestInfoMessage2", "&7Your original state will be restored after the test.", true);
        skillTestCancelNoTestMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when there is no active test to cancel"),
                file, fileConfiguration, "skillTestCancelNoTestMessage", "&cNo active test to cancel.", true);
        skillTestCancelSuccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a skill test is cancelled successfully"),
                file, fileConfiguration, "skillTestCancelSuccessMessage", "&aTest cancelled and player state restored.", true);

        // Debug command
        debugEnabledMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when debug mode is enabled"),
                file, fileConfiguration, "debugEnabledMessage", "&aDebug mode &2enabled&a. Combat damage calculations will be logged to console.", true);
        debugDisabledMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when debug mode is disabled"),
                file, fileConfiguration, "debugDisabledMessage", "&cDebug mode &4disabled&c.", true);

        // Discord command
        discordMessage = ConfigurationEngine.setString(
                List.of("Sets the message showing the Discord link"),
                file, fileConfiguration, "discordMessage", "&6Discord room for support & downloads: &9", true);
        discordMessageSentMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a Discord message is sent"),
                file, fileConfiguration, "discordMessageSentMessage", "&aAttempted to send a message to Discord!", true);

        // Scroll commands
        eliteScrollConfigMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when elite scrolls are not enabled (long config explanation)"),
                file, fileConfiguration, "eliteScrollConfigMessage",
                "Elite Scrolls are not currently enabled on this server! They should only be used if the server uses an item system other than EliteMobs' built in item system. To enable elite scrolls, an admin has to set them to true in the ~/plugins/EliteMobs/ItemSettings.yml and set useEliteItemScrolls to true.", true);
        scrollsNotEnabledMessage = ConfigurationEngine.setString(
                List.of("Sets the short message when elite scrolls are not enabled"),
                file, fileConfiguration, "scrollsNotEnabledMessage",
                "Elite Scrolls are not currently enabled on this server! Ask the admin to enable them in the EliteMobs DefaultConfig.yml", true);
        scrollInvalidNumberMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when scroll level/amount is not a valid integer"),
                file, fileConfiguration, "scrollInvalidNumberMessage", "Level and amount must be valid integers.", true);
        scrollLevelZeroMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when scroll level is zero or negative"),
                file, fileConfiguration, "scrollLevelZeroMessage", "Scroll level must be greater than zero.", true);
        scrollAmountZeroMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when scroll amount is zero or negative"),
                file, fileConfiguration, "scrollAmountZeroMessage", "Amount must be greater than zero.", true);
        scrollGaveMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when scrolls are given to a player",
                        "$amount - the number of scrolls", "$level - the scroll level", "$player - the player name"),
                file, fileConfiguration, "scrollGaveMessage", "Gave $amount level $level Elite Scroll(s) to $player.", true);

        // Other commands
        helpHeaderMessage = ConfigurationEngine.setString(
                List.of("Sets the header message for the help command"),
                file, fileConfiguration, "helpHeaderMessage", "Commands:", true);
        wormholeInvalidOptionMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an invalid wormhole option is specified"),
                file, fileConfiguration, "wormholeInvalidOptionMessage", "Not a valid wormhole option! Pick 1 or 2 to set either end of the wormhole.", true);
        wormholeSetFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when setting a wormhole location fails"),
                file, fileConfiguration, "wormholeSetFailedMessage", "Failed to set location for this wormhole.", true);
        protectionBypassMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when toggling dungeon protection bypass", "$status - the bypass status"),
                file, fileConfiguration, "protectionBypassMessage", "Bypassing dungeon protections is now $status", true);
        setupDoneDisableMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when setup messages are disabled"),
                file, fileConfiguration, "setupDoneDisableMessage", "&aEliteMobs will no longer send messages on login. You can do [/em setup done] again to revert this.", true);
        setupDoneEnableMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when setup messages are re-enabled"),
                file, fileConfiguration, "setupDoneEnableMessage", "&aEliteMobs will once again send messages on login. You can do [/em setup done] again to revert this.", true);
        setupNotValidPackageMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a setup package is not valid"),
                file, fileConfiguration, "setupNotValidPackageMessage", "Not a valid em package!", true);
        setupInstalledMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when content is installed successfully"),
                file, fileConfiguration, "setupInstalledMessage", "Successfully installed content!", true);
        setupUninstalledMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when content is uninstalled successfully"),
                file, fileConfiguration, "setupUninstalledMessage", "Successfully uninstalled content!", true);
        shopPlayerNotFoundMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a shop player is not found"),
                file, fileConfiguration, "shopPlayerNotFoundMessage", "Failed to get player with that username!", true);
        notQueuedForInstanceMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player is not queued for instanced content"),
                file, fileConfiguration, "notQueuedForInstanceMessage", "You are not queued for instanced content!", true);
        reloadStartMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when the plugin starts reloading"),
                file, fileConfiguration, "reloadStartMessage", "Plugin reloading...", true);
        reloadSuccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when the plugin is reloaded"),
                file, fileConfiguration, "reloadSuccessMessage", "Plugin reloaded!", true);
        getTierGaveGearMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when test gear is given", "$level - the tier level"),
                file, fileConfiguration, "getTierGaveGearMessage", "&aGave level $level test gear and set all skills to level $level.", true);
        getTierIronSwordMessage = ConfigurationEngine.setString(
                List.of("Sets the message about the iron sword for balanced testing"),
                file, fileConfiguration, "getTierIronSwordMessage", "&7Use the &fIron Sword &7for balanced damage testing.", true);
        getTierCheatSwordMessage = ConfigurationEngine.setString(
                List.of("Sets the message about the cheat sword"),
                file, fileConfiguration, "getTierCheatSwordMessage", "&7The &cCHEAT SWORD &7has Sharpness 100 - use it only to skip fights!", true);

        // TransitiveBlockCommand messages
        transitiveBlockStartMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when starting large selection for transitive blocks"),
                file, fileConfiguration, "transitiveBlockStartMessage", "[EliteMobs] Now registering large selection for transitive blocks! Left click to set corner 1, right click to set corner 2! Run the same command again to stop registering blocks and run /em cancelblocks to cancel!", true);
        transitiveBlockInvalidBossMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when the boss file is not valid"),
                file, fileConfiguration, "transitiveBlockInvalidBossMessage", "Boss file isn't valid! Try again with a valid filename for your custom boss.", true);
        transitiveBlockNotRegionalMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when the boss file is not for a regional boss"),
                file, fileConfiguration, "transitiveBlockNotRegionalMessage", "&cBoss file isn't for a regional boss! This feature only works for regional bosses.", true);
        transitiveBlockIgnoreWarningMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to ignore the regional boss warning for phase bosses"),
                file, fileConfiguration, "transitiveBlockIgnoreWarningMessage", "&aIgnore this warning if you are setting blocks for a phase boss whose first phase is a regional boss!", true);
        transitiveBlockInvalidTypeMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when the transitive block type is not valid"),
                file, fileConfiguration, "transitiveBlockInvalidTypeMessage", "Not a valid transitive block type, use ON_SPAWN or ON_REMOVE !", true);
        transitiveBlockCorner1Message = ConfigurationEngine.setString(
                List.of("Sets the message sent when corner 1 is set"),
                file, fileConfiguration, "transitiveBlockCorner1Message", "Set corner 1!", true);
        transitiveBlockCorner2Message = ConfigurationEngine.setString(
                List.of("Sets the message sent when corner 2 is set"),
                file, fileConfiguration, "transitiveBlockCorner2Message", "Set corner 2!", true);
        transitiveBlockSelectionCountMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent showing block selection count", "$count - the number of blocks selected", "$limit - the recommended block limit"),
                file, fileConfiguration, "transitiveBlockSelectionCountMessage", "[EliteMobs] Current selection has $count blocks selected. For performance reasons, it is recommended you don't go over $limit blocks!", true);
        transitiveBlockRegisteredMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a block is registered", "$type - the block type"),
                file, fileConfiguration, "transitiveBlockRegisteredMessage", "Registered $type block!", true);
        transitiveBlockRegisteredAirMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when an air block is registered"),
                file, fileConfiguration, "transitiveBlockRegisteredAirMessage", "Registered air block!", true);
        transitiveBlockUnregisteredMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a block is unregistered"),
                file, fileConfiguration, "transitiveBlockUnregisteredMessage", "Unregistered block!", true);
        transitiveBlockTooManyMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when too many blocks are registered at once", "$limit - the recommended block limit"),
                file, fileConfiguration, "transitiveBlockTooManyMessage", "[EliteMobs] You registered more than $limit blocks at once. For performance reasons, it is recommended you keep the selections low. Avoid things like selecting a lot of unnecessary air blocks!", true);
        transitiveBlockRequiresRegionalMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when transitive blocks require a regional boss to be placed"),
                file, fileConfiguration, "transitiveBlockRequiresRegionalMessage", "&c[EliteMobs] Transitive blocks require one Regional Boss from the configuration files to be placed in order to add transitive blocks relative to the spawn coordinates of that boss.", true);
        transitiveBlockCancellingMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when block registration is cancelled"),
                file, fileConfiguration, "transitiveBlockCancellingMessage", "&c[EliteMobs] Cancelling block registration!", true);
        transitiveBlockMultipleBossesMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when multiple regional bosses are found"),
                file, fileConfiguration, "transitiveBlockMultipleBossesMessage", "&c[EliteMobs] Transitive blocks require having ONLY one Regional Boss from the configuration files to be placed in order to add transitive blocks relative to the spawn coordinates of that boss. If there is more than one boss, the plugin can't determine which boss specifically should be getting the transitive blocks", true);
        transitiveBlockMultipleBossesHintMessage = ConfigurationEngine.setString(
                List.of("Sets the hint message about needing one config file per boss"),
                file, fileConfiguration, "transitiveBlockMultipleBossesHintMessage", "&c[EliteMobs] If want multiple bosses to have transitive blocks, you will need one configuration file per boss that has them!", true);
        transitiveBlockNowRegisteringMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when block registration starts", "$type - the transitive block type"),
                file, fileConfiguration, "transitiveBlockNowRegisteringMessage", "&a[EliteMobs] Now registering $type blocks! &2Punch blocks to register air at that location, or right click blocks to register the block you right clicked!", true);
        transitiveBlockRunSameCommandMessage = ConfigurationEngine.setString(
                List.of("Sets the message about running the same command to stop"),
                file, fileConfiguration, "transitiveBlockRunSameCommandMessage", "&6[EliteMobs] Run the same command to stop registering blocks and save!", true);
        transitiveBlockCancelCommandMessage = ConfigurationEngine.setString(
                List.of("Sets the message about running the cancel command"),
                file, fileConfiguration, "transitiveBlockCancelCommandMessage", "&6[EliteMobs] Or run the command /em cancelblocks to cancel the registration!", true);
        transitiveBlockCancelledMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when block registration is successfully cancelled"),
                file, fileConfiguration, "transitiveBlockCancelledMessage", "&c[EliteMobs] Block registration successfully cancelled!", true);
        transitiveBlockNowSavingMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when saving transitive blocks", "$type - the transitive block type"),
                file, fileConfiguration, "transitiveBlockNowSavingMessage", "&a[EliteMobs] Now saving $type blocks!", true);
        transitiveBlockRegisteredCornerMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when blocks are registered between corners", "$count - the number of blocks registered"),
                file, fileConfiguration, "transitiveBlockRegisteredCornerMessage", "Successfully registered $count blocks between your corners!", true);
        transitiveBlockLocationsRegisteredMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when locations are registered correctly"),
                file, fileConfiguration, "transitiveBlockLocationsRegisteredMessage", "Locations registered correctly!", true);
        transitiveBlockLocationsFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when locations fail to commit"),
                file, fileConfiguration, "transitiveBlockLocationsFailedMessage", "Failed to commit locations!", true);

        // LootStats messages
        lootStatsHeader = ConfigurationEngine.setString(
                List.of("Sets the header message for loot stats"),
                file, fileConfiguration, "lootStatsHeader", "[EliteMobs] Item Stats:", true);
        lootStatsAttackSpeed = ConfigurationEngine.setString(
                List.of("Sets the label for item attack speed"),
                file, fileConfiguration, "lootStatsAttackSpeed", "Item attack speed: ", true);
        lootStatsDamage = ConfigurationEngine.setString(
                List.of("Sets the label for item damage"),
                file, fileConfiguration, "lootStatsDamage", "Item damage: ", true);
        lootStatsEdps = ConfigurationEngine.setString(
                List.of("Sets the label for item EDPS"),
                file, fileConfiguration, "lootStatsEdps", "Item EDPS: ", true);
        lootStatsLevel = ConfigurationEngine.setString(
                List.of("Sets the label for item level"),
                file, fileConfiguration, "lootStatsLevel", "Item level: ", true);
        lootStatsBonusEdps = ConfigurationEngine.setString(
                List.of("Sets the label for item bonus EDPS"),
                file, fileConfiguration, "lootStatsBonusEdps", "Item bonus EDPS: ", true);

        // SimLootCommand messages
        simLootPlayerNotFound = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player is not found for sim loot", "$player - the player name"),
                file, fileConfiguration, "simLootPlayerNotFound", "[EliteMobs] Failed to get a player named \"$player\"!", true);
        simLootFinishedMultiple = ConfigurationEngine.setString(
                List.of("Sets the message sent when sim loot finishes running multiple times",
                        "$player - the player name", "$times - the number of times run", "$level - the level"),
                file, fileConfiguration, "simLootFinishedMultiple", "[EliteMobs] Finished running simulation command for player $player $times times at level $level .", true);
        simLootFinishedSingle = ConfigurationEngine.setString(
                List.of("Sets the message sent when sim loot finishes running once",
                        "$player - the player name", "$level - the level"),
                file, fileConfiguration, "simLootFinishedSingle", "[EliteMobs] Finished running simulation command for player $player at level $level .", true);
        simLootCouldNotSend = ConfigurationEngine.setString(
                List.of("Sets the message sent when an item could not be sent to the player", "$player - the player name"),
                file, fileConfiguration, "simLootCouldNotSend", "&c[EliteMobs] Could not send item to player $player - player with this name was not found!", true);

        // StatsCommand extra messages
        trackedBossCountMessage = ConfigurationEngine.setString(
                List.of("Sets the label for tracked boss count"),
                file, fileConfiguration, "trackedBossCountMessage", "Tracked boss count: ", true);
        trackedNpcCountMessage = ConfigurationEngine.setString(
                List.of("Sets the label for tracked NPC count"),
                file, fileConfiguration, "trackedNpcCountMessage", "Tracked NPC count: ", true);
        statsSeparator = ConfigurationEngine.setString(
                List.of("Sets the separator line for the stats command."),
                file, fileConfiguration, "statsSeparator", "&5&m-----------------------------------------------------", true);
        statsVersionHeader = ConfigurationEngine.setString(
                List.of("Sets the version header for the stats command.",
                        "$version is the placeholder for the plugin version."),
                file, fileConfiguration, "statsVersionHeader", "&7[EM] &a&lEliteMobs v. $version stats:", true);
        statsEntityCountMessage = ConfigurationEngine.setString(
                List.of("Sets the entity count message for the stats command.",
                        "$total is the placeholder for total elite entities.",
                        "$aggressive is the placeholder for elite mobs count.",
                        "$passive is the placeholder for super mobs count."),
                file, fileConfiguration, "statsEntityCountMessage", "&7[EM] &2There are currently &l&6$total &f&2EliteMobs mobs entities in the world, of which &a$aggressive &2are Elite Mobs and &a$passive &2are Super Mobs.", true);
        statsBreakdownPrefix = ConfigurationEngine.setString(
                List.of("Sets the breakdown prefix for the stats command."),
                file, fileConfiguration, "statsBreakdownPrefix", "&2Breakdown: &a", true);
        statsHighestThreatMessage = ConfigurationEngine.setString(
                List.of("Sets the highest threat message for the stats command.",
                        "$player is the placeholder for the player name.",
                        "$threat is the placeholder for the threat tier."),
                file, fileConfiguration, "statsHighestThreatMessage", "&7[EM] &2Highest online threat tier: &a$player &2at total threat tier &a$threat", true);
        statsAverageThreatMessage = ConfigurationEngine.setString(
                List.of("Sets the average threat message for the stats command.",
                        "$average is the placeholder for the average threat tier."),
                file, fileConfiguration, "statsAverageThreatMessage", "&7[EM] &2Average threat tier: &a$average", true);

        // NPCInteractions messages
        npcFeatureComingSoonMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a feature is coming soon"),
                file, fileConfiguration, "npcFeatureComingSoonMessage", "[EliteMobs] This feature is coming soon!", true);
        npcCannotRenameMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player tries to rename an NPC"),
                file, fileConfiguration, "npcCannotRenameMessage", "[EliteMobs] You can't rename NPCs using name tags!", true);
        npcFeatureReplacedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a player interacts with a deprecated NPC (enhancer/refiner/smelter)"),
                file, fileConfiguration, "npcFeatureReplacedMessage", "&8[EliteMobs] &cThis feature has been replaced! This NPC should be removed by an admin as soon as possible.", true);
        npcRemoveInstructions = ConfigurationEngine.setString(
                List.of("Sets the instructions sent to admins on how to remove a deprecated NPC"),
                file, fileConfiguration, "npcRemoveInstructions", "&2To remove this NPC, use the command &6/em remove &2and hit the NPC!", true);

        // SharedLootTable messages
        lootVoteSeparator = ConfigurationEngine.setString(
                List.of("Sets the separator line for loot votes"),
                file, fileConfiguration, "lootVoteSeparator", "&e&l---------------------------------------------", true);
        lootVoteMessage = ConfigurationEngine.setString(
                List.of("Sets the loot vote message", "$count - the number of items to vote on"),
                file, fileConfiguration, "lootVoteMessage", "&8[EliteMobs] &6Loot vote! Do &9/em loot &6 to vote on $count items!", true);

        // VersionChecker messages
        versionCheckConnectionWarning = ConfigurationEngine.setString(
                List.of("Sets the warning message when connection to update servers fails"),
                file, fileConfiguration, "versionCheckConnectionWarning", "&8[EliteMobs] &eWarning: Could not connect to update servers. Version checking is currently unavailable. Check your internet connection or try again later.", true);
        versionOutdatedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when the plugin version is outdated"),
                file, fileConfiguration, "versionOutdatedMessage", "&cYour version of EliteMobs is outdated. &aYou can download the latest version from &3&n&ohttps://nightbreak.io/plugin/elitemobs/", true);
        resourcePackUpdatedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when the resource pack has been updated"),
                file, fileConfiguration, "resourcePackUpdatedMessage", "&8[EliteMobs] &cThe EliteMobs resource pack has updated! This means that the current resource pack will not fully work until you restart your server. You only need to restart once!", true);
        contentUpdatesAvailable = ConfigurationEngine.setString(
                List.of("Sets the message header for content updates available", "$count - the number of updates available"),
                file, fileConfiguration, "contentUpdatesAvailable", "&e$count content update(s) available:", true);
        contentUpdateEntry = ConfigurationEngine.setString(
                List.of("Sets the format for each content update entry", "$name - the name of the content"),
                file, fileConfiguration, "contentUpdateEntry", "&e- $name", true);
        dungeonsOutdatedMessage = ConfigurationEngine.setString(
                List.of("Sets the message header for outdated dungeons on player login"),
                file, fileConfiguration, "dungeonsOutdatedMessage", "&cThe following dungeons are outdated:", true);
        versionUseSetupMessage = ConfigurationEngine.setString(
                List.of("Message telling admins to use /em setup to view and update content."),
                file, fileConfiguration, "versionUseSetupMessage", "&7Use &e/em setup &7to view and update, or ", true);
        versionClickDownloadHover = ConfigurationEngine.setString(
                List.of("Hover text on outdated content entries with a download link."),
                file, fileConfiguration, "versionClickDownloadHover", "&9Click to go to download link!", true);
        versionNoDownloadLink = ConfigurationEngine.setString(
                List.of("Suffix shown after an outdated content name when no download link is available."),
                file, fileConfiguration, "versionNoDownloadLink", " &7(no download link available)", true);
        versionDownloadAtMessage = ConfigurationEngine.setString(
                List.of("Message telling admins where to download updates."),
                file, fileConfiguration, "versionDownloadAtMessage", "&8[EliteMobs]&f You can download the update at ", true);
        versionUpdateEasyMessage = ConfigurationEngine.setString(
                List.of("Message telling admins that updating is easy."),
                file, fileConfiguration, "versionUpdateEasyMessage", "&2Updating is quick & easy! ", true);
        versionClickHereLabel = ConfigurationEngine.setString(
                List.of("Clickable 'Click here' label linking to the wiki."),
                file, fileConfiguration, "versionClickHereLabel", "&9&nClick here", true);
        versionInstallInfoMessage = ConfigurationEngine.setString(
                List.of("Message fragment about install info and support."),
                file, fileConfiguration, "versionInstallInfoMessage", " &2for info on how to install updates and ", true);
        versionHereLabel = ConfigurationEngine.setString(
                List.of("Clickable 'here' label linking to Discord support."),
                file, fileConfiguration, "versionHereLabel", "&9&nhere", true);
        versionSupportRoomMessage = ConfigurationEngine.setString(
                List.of("Message fragment about the support room."),
                file, fileConfiguration, "versionSupportRoomMessage", " &2for the support room.", true);
        versionAutoUpdateMessage = ConfigurationEngine.setString(
                List.of("Clickable message to auto-update all content."),
                file, fileConfiguration, "versionAutoUpdateMessage", "&a[Click here to update all content automatically]", true);
        versionAutoUpdateHover = ConfigurationEngine.setString(
                List.of("Hover text for the auto-update button."),
                file, fileConfiguration, "versionAutoUpdateHover", "&eRuns /em updatecontent", true);
        versionOutdatedEntryPrefix = ConfigurationEngine.setString(
                List.of("Prefix for each outdated content entry in the list."),
                file, fileConfiguration, "versionOutdatedEntryPrefix", "&c- ", true);
        versionNightbreakHover = ConfigurationEngine.setString(
                List.of("Hover text for the Nightbreak content link."),
                file, fileConfiguration, "versionNightbreakHover", "Click for Nightbreak link", true);
        versionWikiHover = ConfigurationEngine.setString(
                List.of("Hover text for the wiki link."),
                file, fileConfiguration, "versionWikiHover", "Click for wiki link", true);
        versionDiscordHover = ConfigurationEngine.setString(
                List.of("Hover text for the Discord support link."),
                file, fileConfiguration, "versionDiscordHover", "Discord support link", true);

        // WormholeEntry OP message
        wormholeOpDownloadMessage = ConfigurationEngine.setString(
                List.of("Sets the OP-only message about download links for wormhole content"),
                file, fileConfiguration, "wormholeOpDownloadMessage", "&8[EliteMobs - OP-only message] &fDownload links are available on &9https://magmaguy.itch.io/ &f(free and premium) and &9https://www.patreon.com/magmaguy &f(premium). You can check the difference between the two and get support here: ", true);

        // Download all content command
        downloadAllNoTokenMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when no Nightbreak token is registered for download all"),
                file, fileConfiguration, "downloadAllNoTokenMessage", "&c[EliteMobs] No Nightbreak token registered. Use /nightbreaklogin <token> first.", true);
        downloadAllNothingMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when all available packages are already downloaded"),
                file, fileConfiguration, "downloadAllNothingMessage", "&a[EliteMobs] No new content to download! All available packages are already downloaded.", true);
        downloadAllNoAccessMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when no accessible content is found"),
                file, fileConfiguration, "downloadAllNoAccessMessage", "&c[EliteMobs] No accessible content found. Link your Nightbreak account and ensure you have access.", true);
        downloadAllFoundMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when downloadable packages are found", "$count - the number of packages"),
                file, fileConfiguration, "downloadAllFoundMessage", "&e[EliteMobs] Found $count packages to download. Starting...", true);
        downloadAllProgressMessage = ConfigurationEngine.setString(
                List.of("Sets the message showing download progress",
                        "$current - current package number", "$total - total packages", "$name - package name"),
                file, fileConfiguration, "downloadAllProgressMessage", "&7[EliteMobs] ($current/$total) Downloading: $name...", true);
        downloadAllDownloadedMoreMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a package is downloaded with more remaining",
                        "$name - package name", "$remaining - number remaining"),
                file, fileConfiguration, "downloadAllDownloadedMoreMessage", "&a[EliteMobs] Downloaded $name! &7Please hold on, $remaining more to go...", true);
        downloadAllDownloadedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a package is downloaded (last one)", "$name - package name"),
                file, fileConfiguration, "downloadAllDownloadedMessage", "&a[EliteMobs] Downloaded $name!", true);
        downloadAllFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a package download fails", "$name - package name"),
                file, fileConfiguration, "downloadAllFailedMessage", "&c[EliteMobs] Failed to download: $name", true);
        downloadAllCompleteMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when all downloads are complete",
                        "$completed - number of successful downloads", "$failed - number of failed downloads"),
                file, fileConfiguration, "downloadAllCompleteMessage", "&a[EliteMobs] Download complete! Downloaded: $completed, Failed: $failed", true);
        downloadAllReloadingMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when reloading to apply downloads"),
                file, fileConfiguration, "downloadAllReloadingMessage", "&a[EliteMobs] Reloading to apply downloads...", true);
        downloadAllSetupClickMessage = ConfigurationEngine.setString(
                List.of("Clickable message to download all available content from the setup menu"),
                file, fileConfiguration, "downloadAllSetupClickMessage", "&a[Click to download all available content]", true);
        downloadAllSetupClickHover = ConfigurationEngine.setString(
                List.of("Hover text for the download all button in setup menu"),
                file, fileConfiguration, "downloadAllSetupClickHover", "&eRuns /em downloadall", true);

        // Update content command
        updateNoTokenMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when no Nightbreak token is registered"),
                file, fileConfiguration, "updateNoTokenMessage", "&c[EliteMobs] No Nightbreak token registered. Use /nightbreaklogin <token> first.", true);
        updateAllUpToDateMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when all content is up to date"),
                file, fileConfiguration, "updateAllUpToDateMessage", "&a[EliteMobs] All content is up to date!", true);
        updateFoundOutdatedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when outdated packages are found", "$count - the number of outdated packages"),
                file, fileConfiguration, "updateFoundOutdatedMessage", "&e[EliteMobs] Found $count outdated packages. Starting updates...", true);
        updateCompleteMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when updates are complete",
                        "$completed - number of successful downloads", "$failed - number of failed downloads"),
                file, fileConfiguration, "updateCompleteMessage", "&a[EliteMobs] Update complete! Downloaded: $completed, Failed: $failed", true);
        updateReloadingMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when reloading to apply updates"),
                file, fileConfiguration, "updateReloadingMessage", "&a[EliteMobs] Reloading to apply updates...", true);
        updateProgressMessage = ConfigurationEngine.setString(
                List.of("Sets the message showing download progress",
                        "$current - current package number", "$total - total packages", "$name - package name"),
                file, fileConfiguration, "updateProgressMessage", "&7[EliteMobs] ($current/$total) Downloading: $name...", true);
        updateDownloadedMoreMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a package is downloaded with more remaining",
                        "$name - package name", "$remaining - number remaining"),
                file, fileConfiguration, "updateDownloadedMoreMessage", "&a[EliteMobs] Downloaded $name! &7Please hold on, $remaining more to go...", true);
        updateDownloadedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a package is downloaded (last one)", "$name - package name"),
                file, fileConfiguration, "updateDownloadedMessage", "&a[EliteMobs] Downloaded $name!", true);
        updateFailedMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a package download fails", "$name - package name"),
                file, fileConfiguration, "updateFailedMessage", "&c[EliteMobs] Failed to download: $name", true);

        // FarmingProtection messages
        farmingWarningMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player is farming natural elites too quickly.",
                        "$time is the placeholder for the time until reset."),
                file, fileConfiguration, "farmingWarningMessage", "&c[EliteMobs] &7You've been farming natural elites too quickly! No drops or XP will be awarded. Spread out your farming area or wait for the timeout to reset. Time until reset: &e$time &7seconds.", true);
        farmingResetMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player's natural elite kill limit has been reset."),
                file, fileConfiguration, "farmingResetMessage", "&a[EliteMobs] &7Your natural elite kill limit has been reset!", true);
        farmingCapMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player is temporarily capped from earning rewards.",
                        "$time is the placeholder for the time until reset."),
                file, fileConfiguration, "farmingCapMessage", "&c[EliteMobs] &7You're temporarily capped from earning rewards from natural elites in this area. Time until reset: &e$time &7seconds.", true);

        // SharedLootTable need/greed messages
        lootNeedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player selects need for a loot item.",
                        "$player is the placeholder for the player's display name.",
                        "$item is the placeholder for the item's display name."),
                file, fileConfiguration, "lootNeedMessage", "$player &chas selected need for $item!", true);
        lootGreedMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when a player selects greed for a loot item.",
                        "$player is the placeholder for the player's display name.",
                        "$item is the placeholder for the item's display name."),
                file, fileConfiguration, "lootGreedMessage", "$player &2has selected greed for $item!", true);

        // QuestCommand messages
        questInvalidIdMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when a quest ID is invalid."),
                file, fileConfiguration, "questInvalidIdMessage", "&c[EliteMobs] Invalid quest ID!", true);

        // QuestBypassCommand messages
        questBypassOnMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when quest bypass is enabled."),
                file, fileConfiguration, "questBypassOnMessage", "Now bypassing quest permission requirements!", true);
        questBypassOffMessage = ConfigurationEngine.setString(
                List.of("Sets the message shown when quest bypass is disabled."),
                file, fileConfiguration, "questBypassOffMessage", "No longer bypassing quest permission requirements!", true);

        // CustomTreasureChestsConfig messages
        invalidTreasureChestFilename = ConfigurationEngine.setString(
                List.of("Sets the message shown when a treasure chest file name is invalid."),
                file, fileConfiguration, "invalidTreasureChestFilename", "&c[EliteMobs] Invalid Treasure Chest file name!", true);

        // RelativeCoordinatesCommand messages
        relativePositionMessage = ConfigurationEngine.setString(
                List.of("Sets the message showing the relative position to an anchor point.",
                        "$filename is the placeholder for the dungeon name.",
                        "$coordinates is the placeholder for the relative coordinates."),
                file, fileConfiguration, "relativePositionMessage", "&8[EliteMobs] &fRelative position to anchor point of &2$filename&f is &2$coordinates", true);

        // GetTierCommand display names
        cheatSwordDisplayName = ConfigurationEngine.setString(
                List.of("Display name for the cheat sword given by the get tier command."),
                file, fileConfiguration, "cheatSwordDisplayName", "CHEAT SWORD", true);
    }
}
