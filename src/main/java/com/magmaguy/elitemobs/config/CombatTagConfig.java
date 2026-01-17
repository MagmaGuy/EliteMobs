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
                List.of("Sets if the combat tag is enabled.", "When enabled, flying players that engage in combat are set to stop flying."),
                fileConfiguration, "Enable combat tag", true);
        combatTagMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent when the combat tag is activated."),
                file, fileConfiguration, "Combat tag message", "&c[EliteMobs] Combat tag activated!", true);
        enableTeleportTimer = ConfigurationEngine.setBoolean(
                List.of("Sets if the /ag command will have a timer before teleportation"),
                fileConfiguration, "Enable adventurers guild teleport timer", true);
        teleportTimeLeft = ConfigurationEngine.setString(
                List.of("Sets the action message set while waiting for the teleport timer."),
                file, fileConfiguration, "Teleport time left", "&7[EM] Teleporting in &a$time &7seconds...", true);
        teleportCancelled = ConfigurationEngine.setString(
                List.of("Sets the message sent when players move while waiting for teleportation."),
                file, fileConfiguration, "Teleport cancelled", "&7[EM] &cTeleport interrupted!", true);
        useActionBarMessagesInsteadOfChat = ConfigurationEngine.setBoolean(List.of("Sets whether the action bar or chat will be used for the teleport countdown timer"),
                fileConfiguration, "useActionBarMessagesInsteadOfChat", true);
        preventFlyToggleInDungeons = ConfigurationEngine.setBoolean(
                List.of("Sets whether non-op players are prevented from toggling flight in EliteMobs worlds."),
                fileConfiguration, "Prevent fly toggle in dungeons", true);
    }
}
