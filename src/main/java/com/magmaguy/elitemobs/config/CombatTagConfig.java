package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class CombatTagConfig {
    @Getter
    private static boolean enableCombatTag;
    @Getter
    private static String combatTagMessage;
    @Getter
    private static boolean enableTeleportTimer;

    private CombatTagConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("CombatTag.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        enableCombatTag = ConfigurationEngine.setBoolean(fileConfiguration, "Enable combat tag", true);
        combatTagMessage = ConfigurationEngine.setString(fileConfiguration, "Combat tag message", "&c[EliteMobs] Combat tag activated!");
        enableTeleportTimer = ConfigurationEngine.setBoolean(fileConfiguration, "Enable adventurers guild teleport timer", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
