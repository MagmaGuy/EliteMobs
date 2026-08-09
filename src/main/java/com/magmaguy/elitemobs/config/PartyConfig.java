package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

/**
 * Player-facing settings for session-scoped EliteMobs parties.
 */
public class PartyConfig extends ConfigurationFile {
    @Getter
    private static boolean enabled;
    @Getter
    private static double sharedProgressRange;
    @Getter
    private static int inviteTimeoutSeconds;
    @Getter
    private static int dungeonReadyCheckTimeoutSeconds;
    @Getter
    private static int lootVoteMaximumLifetimeSeconds;
    @Getter
    private static boolean sidebarEnabled;
    @Getter
    private static int sidebarRotationSeconds;
    @Getter
    private static boolean playerInteractionHintsEnabled;
    @Getter
    private static int playerInteractionHintCooldownSeconds;

    @Getter
    private static String prefix;
    @Getter
    private static String unknownPlayerName;
    @Getter
    private static String disabledMessage;
    @Getter
    private static String alreadyInPartyMessage;
    @Getter
    private static String notInPartyMessage;
    @Getter
    private static String partyCreatedMessage;
    @Getter
    private static String partyFullMessage;
    @Getter
    private static String playerUnavailableMessage;
    @Getter
    private static String playerAlreadyInPartyMessage;
    @Getter
    private static String selfInviteMessage;
    @Getter
    private static String inviteSentMessage;
    @Getter
    private static String inviteAlreadyPendingMessage;
    @Getter
    private static String playerCannotUsePartiesMessage;
    @Getter
    private static String inviteReceivedMessage;
    @Getter
    private static String inviteAcceptButton;
    @Getter
    private static String inviteAcceptHover;
    @Getter
    private static String noPendingInviteMessage;
    @Getter
    private static String inviteExpiredMessage;
    @Getter
    private static String joinedPartyMessage;
    @Getter
    private static String memberJoinedMessage;
    @Getter
    private static String leftPartyMessage;
    @Getter
    private static String memberLeftMessage;
    @Getter
    private static String leaderChangedMessage;
    @Getter
    private static String dungeonPartyTooLargeMessage;
    @Getter
    private static String dungeonPartyUnavailableMessage;
    @Getter
    private static String dungeonPartyInInstanceMessage;
    @Getter
    private static String dungeonPartyNoPermissionMessage;
    @Getter
    private static String dungeonPartyChangedMessage;
    @Getter
    private static String dungeonPartyJoinFailedMessage;
    @Getter
    private static String dungeonReadyCheckPendingMessage;
    @Getter
    private static String dungeonReadyCheckStartedMessage;
    @Getter
    private static String dungeonReadyCheckPromptMessage;
    @Getter
    private static String dungeonReadyCheckReadyButton;
    @Getter
    private static String dungeonReadyCheckReadyHover;
    @Getter
    private static String dungeonReadyCheckDeclineButton;
    @Getter
    private static String dungeonReadyCheckDeclineHover;
    @Getter
    private static String dungeonReadyCheckCancelPromptMessage;
    @Getter
    private static String dungeonReadyCheckCancelButton;
    @Getter
    private static String dungeonReadyCheckNoPendingMessage;
    @Getter
    private static String dungeonReadyCheckAlreadyReadyMessage;
    @Getter
    private static String dungeonReadyCheckPlayerReadyMessage;
    @Getter
    private static String dungeonReadyCheckCompleteMessage;
    @Getter
    private static String dungeonReadyCheckDeclinedMessage;
    @Getter
    private static String dungeonReadyCheckExpiredMessage;
    @Getter
    private static String dungeonReadyCheckUnknownDungeonName;
    @Getter
    private static String dungeonReadyCheckLevelFormat;

    @Getter
    private static String sidebarTitle;
    @Getter
    private static String sidebarLeaderLine;
    @Getter
    private static String sidebarMemberLine;
    @Getter
    private static String sidebarQuestLine;
    @Getter
    private static String sidebarHealthGlyph;
    @Getter
    private static String sidebarHealthHealthyColor;
    @Getter
    private static String sidebarHealthWoundedColor;
    @Getter
    private static String sidebarHealthCriticalColor;
    @Getter
    private static String sidebarHealthMissingColor;
    @Getter
    private static String sidebarDownedDisplay;
    @Getter
    private static String sidebarLivesDisplay;
    @Getter
    private static String sidebarInviteAction;
    @Getter
    private static String sidebarLeaveAction;
    @Getter
    private static String playerInteractionHintMessage;
    @Getter
    private static String playerInteractionHintInviteButton;
    @Getter
    private static String playerInteractionHintInviteHover;
    @Getter
    private static String playerInteractionHintDisableButton;
    @Getter
    private static String playerInteractionHintDisableHover;
    @Getter
    private static String playerInteractionHintDisabledMessage;
    @Getter private static String inventoryControlsTitle;
    @Getter private static String inventoryInvitePlayersTitle;
    @Getter private static String inventoryInteractionTitle;
    @Getter private static String inventoryInvitationTitle;
    @Getter private static String inventoryReadyCheckTitle;
    @Getter private static String inventoryInviteName;
    @Getter private static String inventoryInviteLore;
    @Getter private static String inventoryLeaveName;
    @Getter private static String inventoryLeaveLore;
    @Getter private static String inventoryCreateName;
    @Getter private static String inventoryCreateLore;
    @Getter private static String inventoryBackName;
    @Getter private static String inventoryBackToPartyName;
    @Getter private static String inventoryPreviousName;
    @Getter private static String inventoryNextName;
    @Getter private static String inventoryInvitePlayerName;
    @Getter private static String inventorySelectInviteLore;
    @Getter private static String inventoryNoInvitePlayersName;
    @Getter private static String inventoryNoInvitePlayersLore;
    @Getter private static String inventoryInteractionInviteName;
    @Getter private static String inventoryInteractionInviteLore;
    @Getter private static String inventoryNeverShowName;
    @Getter private static String inventoryNeverShowLore;
    @Getter private static String inventoryAcceptInviteName;
    @Getter private static String inventoryAcceptInviteLore;
    @Getter private static String inventoryDeclineName;
    @Getter private static String inventoryDeclineLore;
    @Getter private static String inventoryReadyDungeonName;
    @Getter private static String inventoryReadyWaitingLore;
    @Getter private static String inventoryReadyConfirmLore;
    @Getter private static String inventoryReadyName;
    @Getter private static String inventoryReadyLore;
    @Getter private static String inventoryCancelReadyName;
    @Getter private static String inventoryCancelReadyLore;
    @Getter private static String inventoryNoPermissionMessage;
    @Getter private static String commandCreateDescription;
    @Getter private static String commandInviteDescription;
    @Getter private static String commandMenuDescription;
    @Getter private static String commandAcceptDescription;
    @Getter private static String commandLeaveDescription;
    @Getter private static String commandReadyDescription;
    @Getter private static String commandDeclineDescription;
    @Getter private static String commandHideInteractionHintDescription;
    @Getter private static String readyCheckTokenHint;

    public PartyConfig() {
        super("Party.yml");
    }

    @Override
    public void initializeValues() {
        enabled = ConfigurationEngine.setBoolean(
                List.of("Enables session-scoped player parties.",
                        "Party membership is never saved and ends when players log out or leave."),
                fileConfiguration, "enabled", true);
        sharedProgressRange = Math.max(1D, ConfigurationEngine.setDouble(
                List.of("Maximum same-world distance in blocks for party loot voting and shared quest objective credit.",
                        "This prevents remote or AFK party members from receiving progression."),
                fileConfiguration, "sharedProgressRange", 128D));
        inviteTimeoutSeconds = Math.max(5, ConfigurationEngine.setInt(
                List.of("How long a party invitation remains valid."),
                fileConfiguration, "inviteTimeoutSeconds", 60));
        dungeonReadyCheckTimeoutSeconds = Math.max(5, ConfigurationEngine.setInt(
                List.of("How long party members have to accept an instanced dungeon ready check."),
                fileConfiguration, "dungeonReadyCheckTimeoutSeconds", 30));
        lootVoteMaximumLifetimeSeconds = Math.max(60, ConfigurationEngine.setInt(
                List.of("Hard maximum lifetime for one party need/greed vote session.",
                        "New party drops still refresh the normal 60-second voting window, but can never extend a session past this limit."),
                fileConfiguration, "lootVoteMaximumLifetimeSeconds", 120));
        sidebarEnabled = ConfigurationEngine.setBoolean(
                List.of("Shows the combined party status and tracked quest sidebar while players are in a party.",
                        "Disabling this keeps parties active and falls back to the normal EliteMobs quest scoreboard."),
                fileConfiguration, "sidebarEnabled", true);
        sidebarRotationSeconds = Math.max(3, ConfigurationEngine.setInt(
                List.of("How often the party sidebar alternates between its invite and leave command hints."),
                fileConfiguration, "sidebarRotationSeconds", 10));
        playerInteractionHintsEnabled = ConfigurationEngine.setBoolean(
                List.of("Shows a short party invitation hint when a player right-clicks another player.",
                        "This has no effect when the party system itself is disabled."),
                fileConfiguration, "playerInteractionHintsEnabled", true);
        playerInteractionHintCooldownSeconds = Math.max(1, ConfigurationEngine.setInt(
                List.of("Minimum time before a player can receive another right-click party hint."),
                fileConfiguration, "playerInteractionHintCooldownSeconds", 300));

        migrateDefault("sidebarLeaderLine", "&6♛ &f$player", "&6♛ &f$player$health$lives");
        migrateDefault("sidebarMemberLine", "&a● &f$player", "&a● &f$player$health$lives");
        migrateWipVisualDefaults();

        prefix = message("prefix", "&8[<g:#A04468:#E07A9A>Party</g>&8] &7");
        unknownPlayerName = message("unknownPlayerName", "Unknown");
        disabledMessage = message("disabledMessage", "$prefixThe party system is disabled on this server.");
        alreadyInPartyMessage = message("alreadyInPartyMessage", "$prefixYou are already in a party.");
        notInPartyMessage = message("notInPartyMessage", "$prefixYou are not in a party.");
        partyCreatedMessage = message("partyCreatedMessage", "$prefix&aParty created! &7Invite players with &f/em party invite <player>&7.");
        migrateDefault("partyFullMessage",
                "$prefixThat party already has six players.",
                "$prefixThat party already has five players.");
        partyFullMessage = message("partyFullMessage", "$prefixThat party already has five players.");
        playerUnavailableMessage = message("playerUnavailableMessage", "$prefixThat player is not online.");
        playerAlreadyInPartyMessage = message("playerAlreadyInPartyMessage", "$prefixThat player is already in a party.");
        selfInviteMessage = message("selfInviteMessage", "$prefixYou cannot invite yourself.");
        inviteSentMessage = message("inviteSentMessage", "$prefixInvitation sent to &f$player&7.");
        inviteAlreadyPendingMessage = message("inviteAlreadyPendingMessage",
                "$prefix&f$player &7already has a pending party invitation.");
        playerCannotUsePartiesMessage = message("playerCannotUsePartiesMessage",
                "$prefix&f$player &7does not have permission to use parties.");
        inviteReceivedMessage = message("inviteReceivedMessage", "$prefix&f$player &7invited you to join their party. ");
        inviteAcceptButton = message("inviteAcceptButton", "<g:#2E7D4F:#69C56F>[ACCEPT]</g>");
        inviteAcceptHover = message("inviteAcceptHover", "&7Click to join the party");
        noPendingInviteMessage = message("noPendingInviteMessage", "$prefixYou do not have a pending party invitation.");
        inviteExpiredMessage = message("inviteExpiredMessage", "$prefixThat party invitation expired.");
        joinedPartyMessage = message("joinedPartyMessage", "$prefix&aYou joined the party!");
        memberJoinedMessage = message("memberJoinedMessage", "$prefix&f$player &ajoined the party.");
        leftPartyMessage = message("leftPartyMessage", "$prefixYou left the party.");
        memberLeftMessage = message("memberLeftMessage", "$prefix&f$player &cleft the party.");
        leaderChangedMessage = message("leaderChangedMessage", "$prefix&f$player &7is now the party leader.");
        dungeonPartyTooLargeMessage = message("dungeonPartyTooLargeMessage",
                "$prefixParty entry needs &f$count &7open spots, but this dungeon only has &f$max &7available.");
        dungeonPartyUnavailableMessage = message("dungeonPartyUnavailableMessage",
                "$prefixParty entry stopped because &f$player &7is no longer available.");
        dungeonPartyInInstanceMessage = message("dungeonPartyInInstanceMessage",
                "$prefixParty entry stopped because &f$player &7is already in another instance.");
        dungeonPartyNoPermissionMessage = message("dungeonPartyNoPermissionMessage",
                "$prefixParty entry stopped because &f$player &7does not have permission for this dungeon.");
        dungeonPartyChangedMessage = message("dungeonPartyChangedMessage",
                "$prefixThe party changed while the dungeon was being prepared. Please try again.");
        dungeonPartyJoinFailedMessage = message("dungeonPartyJoinFailedMessage",
                "$prefixThe party could not enter together, so nobody was moved.");
        dungeonReadyCheckPendingMessage = message("dungeonReadyCheckPendingMessage",
                "$prefixYour party already has a dungeon ready check in progress.");
        dungeonReadyCheckStartedMessage = message("dungeonReadyCheckStartedMessage",
                "$prefix&f$player &7wants to enter &f$dungeon&7. Waiting for the party (&f$ready&7/&f$total&7).");
        dungeonReadyCheckPromptMessage = message("dungeonReadyCheckPromptMessage",
                "$prefixReady to enter &f$dungeon&7? ");
        dungeonReadyCheckReadyButton = message("dungeonReadyCheckReadyButton", "<g:#2E7D4F:#69C56F>[READY]</g>");
        dungeonReadyCheckReadyHover = message("dungeonReadyCheckReadyHover", "&7Click to confirm dungeon entry");
        dungeonReadyCheckDeclineButton = message("dungeonReadyCheckDeclineButton", " <g:#7A1F2B:#C2414A>[DECLINE]</g>");
        dungeonReadyCheckDeclineHover = message("dungeonReadyCheckDeclineHover", "&7Click to cancel this party entry");
        dungeonReadyCheckCancelPromptMessage = message("dungeonReadyCheckCancelPromptMessage",
                "$prefixStarted the wrong dungeon? ");
        dungeonReadyCheckCancelButton = message("dungeonReadyCheckCancelButton", "<g:#7A1F2B:#C2414A>[CANCEL]</g>");
        dungeonReadyCheckNoPendingMessage = message("dungeonReadyCheckNoPendingMessage",
                "$prefixThat dungeon ready check is no longer active.");
        dungeonReadyCheckAlreadyReadyMessage = message("dungeonReadyCheckAlreadyReadyMessage",
                "$prefixYou are already ready for this dungeon.");
        dungeonReadyCheckPlayerReadyMessage = message("dungeonReadyCheckPlayerReadyMessage",
                "$prefix&f$player &ais ready &7(&f$ready&7/&f$total&7).");
        dungeonReadyCheckCompleteMessage = message("dungeonReadyCheckCompleteMessage",
                "$prefix&aEveryone is ready! &7Preparing &f$dungeon&7...");
        dungeonReadyCheckDeclinedMessage = message("dungeonReadyCheckDeclinedMessage",
                "$prefix&f$player &cdeclined &7the ready check for &f$dungeon&7.");
        dungeonReadyCheckExpiredMessage = message("dungeonReadyCheckExpiredMessage",
                "$prefixThe ready check for &f$dungeon &7expired.");
        dungeonReadyCheckUnknownDungeonName = message("dungeonReadyCheckUnknownDungeonName", "this dungeon");
        dungeonReadyCheckLevelFormat = message("dungeonReadyCheckLevelFormat", "$dungeon - level $level");

        sidebarTitle = message("sidebarTitle", "<g:#A04468:#E07A9A>♥ Elite Party</g>");
        sidebarLeaderLine = message("sidebarLeaderLine", "&6★ &f$player$health$lives");
        sidebarMemberLine = message("sidebarMemberLine", "&a● &f$player$health$lives");
        sidebarQuestLine = message("sidebarQuestLine", "&bQuest: &f$quest");
        sidebarHealthGlyph = message("sidebarHealthGlyph", "❤");
        sidebarHealthHealthyColor = message("sidebarHealthHealthyColor", "&a");
        sidebarHealthWoundedColor = message("sidebarHealthWoundedColor", "&e");
        sidebarHealthCriticalColor = message("sidebarHealthCriticalColor", "&c");
        sidebarHealthMissingColor = message("sidebarHealthMissingColor", "&8");
        sidebarDownedDisplay = message("sidebarDownedDisplay", " &c☠");
        sidebarLivesDisplay = message("sidebarLivesDisplay", " &b◆&f$lives");
        sidebarInviteAction = message("sidebarInviteAction", "&b✉ &f/em party invite <player>");
        sidebarLeaveAction = message("sidebarLeaveAction", "&c✘ &f/em party leave");
        playerInteractionHintMessage = message("playerInteractionHintMessage",
                "$prefixWant to team up with &f$player&7? ");
        playerInteractionHintInviteButton = message("playerInteractionHintInviteButton",
                "<g:#2E7D4F:#69C56F>[INVITE]</g>");
        playerInteractionHintInviteHover = message("playerInteractionHintInviteHover",
                "&7Invite &f$player &7to your party");
        playerInteractionHintDisableButton = message("playerInteractionHintDisableButton", "&8[Don't show again]");
        playerInteractionHintDisableHover = message("playerInteractionHintDisableHover",
                "&7Permanently hide right-click party hints");
        playerInteractionHintDisabledMessage = message("playerInteractionHintDisabledMessage",
                "$prefixRight-click party hints have been disabled for you.");
        inventoryControlsTitle = message("inventoryControlsTitle", "<g:#A04468:#E07A9A>♥ Elite Party</g>");
        inventoryInvitePlayersTitle = message("inventoryInvitePlayersTitle", "<g:#A04468:#E07A9A>✉ Invite a Player</g>");
        inventoryInteractionTitle = message("inventoryInteractionTitle", "<g:#A04468:#E07A9A>♥ Party with</g> &f$player");
        inventoryInvitationTitle = message("inventoryInvitationTitle", "<g:#A04468:#E07A9A>✉ Party Invitation</g>");
        inventoryReadyCheckTitle = message("inventoryReadyCheckTitle", "<g:#6D3AA8:#A855F7>★ Dungeon Ready Check</g>");
        inventoryInviteName = message("inventoryInviteName", "<g:#267A78:#58B8A9>✉ Invite a Player</g>");
        inventoryInviteLore = message("inventoryInviteLore", "&7Choose an online player to invite.");
        inventoryLeaveName = message("inventoryLeaveName", "<g:#7A1F2B:#C2414A>✘ Leave Party</g>");
        inventoryLeaveLore = message("inventoryLeaveLore", "&7Leave your current party.");
        inventoryCreateName = message("inventoryCreateName", "<g:#2E7D4F:#69C56F>+ Create a Party</g>");
        inventoryCreateLore = message("inventoryCreateLore", "&7Create a party for up to five players.");
        inventoryBackName = message("inventoryBackName", "&eBack to /em");
        inventoryBackToPartyName = message("inventoryBackToPartyName", "&eBack to Party Controls");
        inventoryPreviousName = message("inventoryPreviousName", "&ePrevious Page");
        inventoryNextName = message("inventoryNextName", "&eNext Page");
        inventoryInvitePlayerName = message("inventoryInvitePlayerName", "<g:#267A78:#58B8A9>✉ Invite</g> &f$player");
        inventorySelectInviteLore = message("inventorySelectInviteLore", "&7Click to invite.");
        inventoryNoInvitePlayersName = message("inventoryNoInvitePlayersName", "&7No Players Available");
        inventoryNoInvitePlayersLore = message("inventoryNoInvitePlayersLore", "&8Nobody online can currently be invited.");
        inventoryInteractionInviteName = message("inventoryInteractionInviteName", "<g:#267A78:#58B8A9>✉ Invite</g> &f$player");
        inventoryInteractionInviteLore = message("inventoryInteractionInviteLore", "&7Invite this player to your party.");
        inventoryNeverShowName = message("inventoryNeverShowName", "&8Never Show This Again");
        inventoryNeverShowLore = message("inventoryNeverShowLore", "&7Permanently hide right-click party hints.");
        inventoryAcceptInviteName = message("inventoryAcceptInviteName", "<g:#2E7D4F:#69C56F>Accept Invite</g> &f— $player");
        inventoryAcceptInviteLore = message("inventoryAcceptInviteLore", "&7Join their EliteMobs party.");
        inventoryDeclineName = message("inventoryDeclineName", "<g:#7A1F2B:#C2414A>Decline</g>");
        inventoryDeclineLore = message("inventoryDeclineLore", "&7Ignore this invitation.");
        inventoryReadyDungeonName = message("inventoryReadyDungeonName", "<g:#6D3AA8:#A855F7>★ Dungeon</g> &f$dungeon");
        inventoryReadyWaitingLore = message("inventoryReadyWaitingLore", "&7Waiting for your party.");
        inventoryReadyConfirmLore = message("inventoryReadyConfirmLore", "&7Confirm that you are ready to enter.");
        inventoryReadyName = message("inventoryReadyName", "<g:#2E7D4F:#69C56F>Ready</g>");
        inventoryReadyLore = message("inventoryReadyLore", "&7Enter when everyone is ready.");
        inventoryCancelReadyName = message("inventoryCancelReadyName", "<g:#7A1F2B:#C2414A>Cancel Ready Check</g>");
        inventoryCancelReadyLore = message("inventoryCancelReadyLore", "&7Cancel this party dungeon entry.");
        inventoryNoPermissionMessage = message("inventoryNoPermissionMessage",
                "&cYou do not have permission to use EliteMobs parties.");
        commandCreateDescription = message("commandCreateDescription", "Creates a session-scoped EliteMobs party.");
        commandInviteDescription = message("commandInviteDescription", "Invites an online player to your EliteMobs party.");
        commandMenuDescription = message("commandMenuDescription", "Opens compatible EliteMobs party controls.");
        commandAcceptDescription = message("commandAcceptDescription", "Accepts your pending EliteMobs party invitation.");
        commandLeaveDescription = message("commandLeaveDescription", "Leaves your current EliteMobs party.");
        commandReadyDescription = message("commandReadyDescription", "Accepts an active party dungeon ready check.");
        commandDeclineDescription = message("commandDeclineDescription", "Declines an active party dungeon ready check.");
        commandHideInteractionHintDescription = message("commandHideInteractionHintDescription",
                "Permanently hides right-click party invitation hints.");
        readyCheckTokenHint = message("readyCheckTokenHint", "ready check token");
    }

    /** Updates only unreleased stock visuals, preserving every customized or translated value. */
    private void migrateWipVisualDefaults() {
        migrateDefault("prefix", "&8[&6Party&8] &7", "&8[<g:#A04468:#E07A9A>Party</g>&8] &7");
        migrateDefault("inviteAcceptButton", "&a&l[ACCEPT]", "<g:#2E7D4F:#69C56F>[ACCEPT]</g>");
        migrateDefault("dungeonReadyCheckReadyButton", "&a&l[READY]", "<g:#2E7D4F:#69C56F>[READY]</g>");
        migrateDefault("dungeonReadyCheckDeclineButton", " &c&l[DECLINE]", " <g:#7A1F2B:#C2414A>[DECLINE]</g>");
        migrateDefault("dungeonReadyCheckCancelButton", "&c&l[CANCEL]", "<g:#7A1F2B:#C2414A>[CANCEL]</g>");
        migrateDefault("playerInteractionHintInviteButton", "&a&l[INVITE]",
                "<g:#2E7D4F:#69C56F>[INVITE]</g>");
        migrateDefault("sidebarTitle", "&6&lElite Party", "<g:#A04468:#E07A9A>♥ Elite Party</g>");
        migrateDefault("sidebarLeaderLine", "&6♛ &f$player$health$lives",
                "&6★ &f$player$health$lives");
        migrateDefault("sidebarLivesDisplay", " &b✦&f$lives", " &b◆&f$lives");
        migrateDefault("sidebarInviteAction", "&a➕ &f/em party invite <player>",
                "&b✉ &f/em party invite <player>");
        migrateDefault("sidebarLeaveAction", "&c✖ &f/em party leave",
                "&c✘ &f/em party leave");
        migrateDefault("inventoryControlsTitle", "&6Elite Party", "<g:#A04468:#E07A9A>♥ Elite Party</g>");
        migrateDefault("inventoryInvitePlayersTitle", "&6Invite a Player", "<g:#A04468:#E07A9A>✉ Invite a Player</g>");
        migrateDefault("inventoryInteractionTitle", "&6Party with $player", "<g:#A04468:#E07A9A>♥ Party with</g> &f$player");
        migrateDefault("inventoryInvitationTitle", "&6Party Invitation", "<g:#A04468:#E07A9A>✉ Party Invitation</g>");
        migrateDefault("inventoryReadyCheckTitle", "&6Dungeon Ready Check", "<g:#6D3AA8:#A855F7>★ Dungeon Ready Check</g>");
        migrateDefault("inventoryInviteName", "&aInvite a Player", "<g:#267A78:#58B8A9>✉ Invite a Player</g>");
        migrateDefault("inventoryLeaveName", "&cLeave Party", "<g:#7A1F2B:#C2414A>✘ Leave Party</g>");
        migrateDefault("inventoryCreateName", "&aCreate a Party", "<g:#2E7D4F:#69C56F>+ Create a Party</g>");
        migrateDefault("inventoryInvitePlayerName", "&a$player", "<g:#267A78:#58B8A9>✉ Invite</g> &f$player");
        migrateDefault("inventoryInteractionInviteName", "&aInvite $player", "<g:#267A78:#58B8A9>✉ Invite</g> &f$player");
        migrateDefault("inventoryAcceptInviteName", "&aAccept $player's Invite", "<g:#2E7D4F:#69C56F>Accept Invite</g> &f— $player");
        migrateDefault("inventoryDeclineName", "&cDecline", "<g:#7A1F2B:#C2414A>Decline</g>");
        migrateDefault("inventoryReadyDungeonName", "&e$dungeon", "<g:#6D3AA8:#A855F7>★ Dungeon</g> &f$dungeon");
        migrateDefault("inventoryReadyName", "&aReady", "<g:#2E7D4F:#69C56F>Ready</g>");
        migrateDefault("inventoryCancelReadyName", "&cCancel Ready Check", "<g:#7A1F2B:#C2414A>Cancel Ready Check</g>");
    }

    private String message(String key, String defaultValue) {
        return ConfigurationEngine.setString(List.of(), file, fileConfiguration, key, defaultValue, true);
    }

    private void migrateDefault(String key, String previousDefault, String currentDefault) {
        if (previousDefault.equals(fileConfiguration.getString(key)))
            fileConfiguration.set(key, currentDefault);
    }
}
