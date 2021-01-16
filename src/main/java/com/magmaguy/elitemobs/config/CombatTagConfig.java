package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class CombatTagConfig {

    public static boolean enableCombatTag;
    public static String combatTagMessage;
    public static boolean enableTeleportTimer;

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("CombatTag.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        enableCombatTag = ConfigurationEngine.setBoolean(fileConfiguration, "Enable combat tag", true);
        combatTagMessage = ConfigurationEngine.setString(fileConfiguration, "Combat tag message", "&c[EliteMobs] Combat tag activated!");
        enableTeleportTimer = ConfigurationEngine.setBoolean(fileConfiguration, "Enable adventurers guild teleport timer", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
