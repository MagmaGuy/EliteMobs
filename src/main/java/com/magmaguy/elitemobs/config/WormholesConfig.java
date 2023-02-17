package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class WormholesConfig {
    @Getter
    private static String dungeonNotInstalledMessage;
    @Getter
    private static String defaultPortalMissingMessage;
    @Getter
    private static boolean reducedParticlesMode;
    @Getter
    private static boolean noParticlesMode;
    @Getter
    private static String insufficientCurrencyForWormholeMessage;

    private WormholesConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("Wormholes.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        dungeonNotInstalledMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when a teleport is used for a dungeon that is not installed."),
                file, fileConfiguration, "dungeonNotInstalledMessage", "&8[EliteMobs] &cDungeon $dungeonID &cis not installed! This teleport will not work.", true);
        defaultPortalMissingMessage = ConfigurationEngine.setString(
                List.of("Sets the message that appears when a wormhole is used for a dungeon that is not installed."),
                file, fileConfiguration, "defaultPortalMissingMessage", "&8[EliteMobs] &cThis portal doesn't seem to lead anywhere!", true);
        reducedParticlesMode = ConfigurationEngine.setBoolean(
                List.of("Sets if the reduced particles mode for wormholes is used. This is especially recommended if you are allowing bedrock clients in."),
                fileConfiguration, "reducedParticlesMode", true);
        noParticlesMode = ConfigurationEngine.setBoolean(
                List.of("Sets if wormholes don't use particles at all. Not recommended, but might be necessary for really bad bedrock clients."),
                fileConfiguration, "noParticlesMode", false);
        insufficientCurrencyForWormholeMessage = ConfigurationEngine.setString(
                List.of("Sets the message that is sent when a player tries to use a wormhole but does not have enough currency to use it."),
                file, fileConfiguration, "insufficientCurrencyForWormholeMessage", "&8[EliteMobs] &cInsufficient currency! You need $amount to use this wormhole!", true);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }
}
