package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

public class CombatTagConfig extends ConfigurationFile {
    @Getter
    private static boolean enableCombatTag;
    @Getter
    private static String combatTagMessage;
    @Getter
    private static boolean enableTeleportTimer;
    @Getter
    private static String teleportTimeLeft;
    @Getter
    private static String teleportCancelled;
    @Getter
    private static boolean useActionBarMessagesInsteadOfChat;
    @Getter
    private static boolean preventFlyToggleInDungeons;

    public CombatTagConfig() {
        super("CombatTag.yml");
    }

    @Override
    public void initializeValues() {
        enableCombatTag = ConfigurationEngine.setBoolean(
                List.of("Whether the combat tag is enabled. Flying players who enter combat are forced to stop flying."),
                fileConfiguration, "enableCombatTag", true);
        combatTagMessage = ConfigurationEngine.setString(
                List.of("Message shown when a player's combat tag activates."),
                file, fileConfiguration, "combatTagMessage", "&c[EliteMobs] Combat tag activated!", true);
        enableTeleportTimer = ConfigurationEngine.setBoolean(
                List.of("Whether the /ag teleport has a countdown timer before teleporting."),
                fileConfiguration, "enableAdventurersGuildTeleportTimer", true);
        teleportTimeLeft = ConfigurationEngine.setString(
                List.of("Action bar message shown during the teleport countdown.",
                        "Placeholder: $time"),
                file, fileConfiguration, "teleportTimeLeft", "&7[EM] Teleporting in &a$time &7seconds...", true);
        teleportCancelled = ConfigurationEngine.setString(
                List.of("Message shown when a player moves and cancels a pending teleport."),
                file, fileConfiguration, "teleportCancelled", "&7[EM] &cTeleport interrupted!", true);
        useActionBarMessagesInsteadOfChat = ConfigurationEngine.setBoolean(
                List.of("Whether to show the teleport countdown in the action bar instead of chat."),
                fileConfiguration, "useActionBarMessagesInsteadOfChat", true);
        preventFlyToggleInDungeons = ConfigurationEngine.setBoolean(
                List.of("Whether non-OP players are prevented from toggling flight in EliteMobs worlds."),
                fileConfiguration, "preventFlyToggleInDungeons", true);
    }
}
