package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;

import java.util.List;

public class SoundsConfig extends ConfigurationFile {
    public static String treasureChestOpenSound;
    public static String guildRankUpSound;
    public static String guildPrestigeSound;
    public static String questProgressionSound;
    public static String questCompleteSound;
    public static String questAbandonSound;
    public static String questAcceptSound;

    public SoundsConfig() {
        super("Sounds.yml");
    }

    @Override
    public void initializeValues() {
        treasureChestOpenSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a treasure chest opens"),
                file, fileConfiguration, "treasureChestOpenSound", "elitemobs:treasure_chest.open", false);

        guildRankUpSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player ranks up at the guild"),
                file, fileConfiguration, "guildRankUpSound", "elitemobs:guild.rankup", false);

        guildPrestigeSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player prestiges at the guild"),
                file, fileConfiguration, "guildPrestigeSound", "elitemobs:guild.prestige", false);

        questProgressionSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player progresses a quest"),
                file, fileConfiguration, "questProgressionSound", "elitemobs:quest.progression", false);

        questCompleteSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player completes a quest"),
                file, fileConfiguration, "questCompleteSound", "elitemobs:quest.completion", false);

        questAbandonSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player abandons a quest"),
                file, fileConfiguration, "questAbandonSound", "elitemobs:quest.abandon", false);

        questAcceptSound = ConfigurationEngine.setString(
                List.of("Sets the sound that will play when a player accepts a quest"),
                file, fileConfiguration, "questAcceptSound", "elitemobs:quest.accept", false);
    }
}
