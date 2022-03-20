package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class WormholesConfig {
    @Getter
    private static String dungeonNotInstalledMessage;
    @Getter
    private static String defaultPortalMissingMessage;
    @Getter
    private static boolean reducedParticlesMode;
    @Getter
    private static boolean noParticlesMode;

    private WormholesConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("Wormholes.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        dungeonNotInstalledMessage = ConfigurationEngine.setString(fileConfiguration, "dungeonNotInstalledMessage", "&8[EliteMobs] &cDungeon $dungeonID &cis not installed! This teleport will not work.");
        defaultPortalMissingMessage = ConfigurationEngine.setString(fileConfiguration, "defaultPortalMissingMessage", "&8[EliteMobs] &cThis portal doesn't seem to lead anywhere!");
        reducedParticlesMode = ConfigurationEngine.setBoolean(fileConfiguration, "reducedParticlesMode", false);
        noParticlesMode = ConfigurationEngine.setBoolean(fileConfiguration, "noParticlesMode", false);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }
}
