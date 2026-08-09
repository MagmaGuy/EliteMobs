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
    private static int sidebarRotationSeconds;

    @Getter
    private static String prefix;
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
        sidebarRotationSeconds = Math.max(3, ConfigurationEngine.setInt(
                List.of("How often the party sidebar alternates between its invite and leave command hints."),
                fileConfiguration, "sidebarRotationSeconds", 10));

        prefix = message("prefix", "&8[&6Party&8] &7");
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
        inviteReceivedMessage = message("inviteReceivedMessage", "$prefix&f$player &7invited you to join their party. ");
        inviteAcceptButton = message("inviteAcceptButton", "&a&l[ACCEPT]");
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

        sidebarTitle = message("sidebarTitle", "&6&lElite Party");
        migrateDefault("sidebarLeaderLine", "&6♛ &f$player", "&6♛ &f$player$health$lives");
        migrateDefault("sidebarMemberLine", "&a● &f$player", "&a● &f$player$health$lives");
        sidebarLeaderLine = message("sidebarLeaderLine", "&6♛ &f$player$health$lives");
        sidebarMemberLine = message("sidebarMemberLine", "&a● &f$player$health$lives");
        sidebarQuestLine = message("sidebarQuestLine", "&bQuest: &f$quest");
        sidebarHealthGlyph = message("sidebarHealthGlyph", "❤");
        sidebarHealthHealthyColor = message("sidebarHealthHealthyColor", "&a");
        sidebarHealthWoundedColor = message("sidebarHealthWoundedColor", "&e");
        sidebarHealthCriticalColor = message("sidebarHealthCriticalColor", "&c");
        sidebarHealthMissingColor = message("sidebarHealthMissingColor", "&8");
        sidebarDownedDisplay = message("sidebarDownedDisplay", " &c☠");
        sidebarLivesDisplay = message("sidebarLivesDisplay", " &b✦&f$lives");
        sidebarInviteAction = message("sidebarInviteAction", "&a➕ &f/em party invite <player>");
        sidebarLeaveAction = message("sidebarLeaveAction", "&c✖ &f/em party leave");
    }

    private String message(String key, String defaultValue) {
        return ConfigurationEngine.setString(List.of(), file, fileConfiguration, key, defaultValue, true);
    }

    private void migrateDefault(String key, String previousDefault, String currentDefault) {
        if (previousDefault.equals(fileConfiguration.getString(key)))
            fileConfiguration.set(key, currentDefault);
    }
}
