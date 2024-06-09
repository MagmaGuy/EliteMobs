package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

public class SoundsConfig {
    private SoundsConfig() {
    }

    public static String treasureChestOpenSound;
    public static String guildRankUpSound;
    public static String guildPrestigeSound;
    public static String questProgressionSound;
    public static String questCompleteSound;
    public static String questAbandonSound;
    public static String questAcceptSound;

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("Sounds.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        treasureChestOpenSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a treasure chest opens"),
                file, fileConfiguration, "treasureChestOpenSound", "elitemobs_treasure_chest_open", false);

        guildRankUpSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player ranks up at the guild"),
                file, fileConfiguration, "guildRankUpSound", "elitemobs_guild_rankup", false);

        guildPrestigeSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player prestiges at the guild"),
                file, fileConfiguration, "guildPrestigeSound", "elitemobs_guild_prestige", false);

        questProgressionSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player progresses a quest"),
                file, fileConfiguration, "questProgressionSound", "elitemobs_quest_progression", false);

        questCompleteSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player completes a quest"),
                file, fileConfiguration, "questCompleteSound", "elitemobs_quest_completion", false);

        questAbandonSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player abandons a quest"),
                file, fileConfiguration, "questAbandonSound", "elitemobs_quest_abandon", false);

        questAcceptSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player accepts a quest"),
                file, fileConfiguration, "questAcceptSound", "elitemobs_quest_accept", false);

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
