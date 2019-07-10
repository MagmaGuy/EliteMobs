package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class CombatTagConfig {

    public static boolean enableCombatTag;
    public static String combatTagMessage;
    public static boolean enableTeleportTimer;
    public static int teleportTimerDuration;

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("CombatTag.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        enableCombatTag = ConfigurationEngine.setBoolean(fileConfiguration, "Enable combat tag", true);
        combatTagMessage = ConfigurationEngine.setString(fileConfiguration, "Combat tag message", "&c[EliteMobs] Combat tag activated!");
        enableTeleportTimer = ConfigurationEngine.setBoolean(fileConfiguration, "Enable adventurers guild teleport timer", true);
        teleportTimerDuration = ConfigurationEngine.setInt(fileConfiguration, "Teleport timer duration", 5);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
