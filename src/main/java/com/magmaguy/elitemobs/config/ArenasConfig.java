package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ArenasConfig {
    @Getter
    private static String notEnoughPlayersMessage;
    @Getter
    private static String startingTitle;
    @Getter
    private static String startingSubtitle;
    @Getter
    private static String arenaFullMessage;
    @Getter
    private static String arenasOngoingMessage;
    @Getter
    private static String arenaStartHintMessage;
    @Getter
    private static String arenaQuitHintMessage;
    @Getter
    private static String arenaJoinPlayerMessage;
    @Getter
    private static String arenaJoinSpectatorMessage;
    @Getter
    private static String waveTitle;
    @Getter
    private static String waveSubtitle;
    @Getter
    private static String victoryTitle;
    @Getter
    private static String victorySubtitle;
    @Getter
    private static String defeatTitle;
    @Getter
    private static String defeatSubtitle;
    @Getter
    private static String victoryBroadcast;
    @Getter
    private static String joinPlayerTitle;
    @Getter
    private static String joinPlayerSubtitle;
    @Getter
    private static String joinSpectatorTitle;
    @Getter
    private static String joinSpectatorSubtitle;

    private ArenasConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("Arenas.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        notEnoughPlayersMessage = ConfigurationEngine.setString(fileConfiguration, "notEnoughPlayersMessage", "&8[EliteMobs] &cYou need at least $amount to start the match!");
        startingTitle = ConfigurationEngine.setString(fileConfiguration, "startingMessage", "&2Starting!");
        startingSubtitle = ConfigurationEngine.setString(fileConfiguration, "startingSubtitle", "&2in $count...");
        arenaFullMessage = ConfigurationEngine.setString(fileConfiguration, "arenaFullMessage", "&4[EliteMobs] &cArena is full! You can spectate instead while you wait for it to finish!");
        arenasOngoingMessage = ConfigurationEngine.setString(fileConfiguration, "arenasOngoingMessage", "&4[EliteMobs] &cCan't join the arena now - a match is currently happening! You can spectate instead while you wait for it to finish!");
        arenaStartHintMessage = ConfigurationEngine.setString(fileConfiguration, "arenaStartHintMessage", "&2[EliteMobs] &aYou can start the arena by doing &2/em start");
        arenaQuitHintMessage = ConfigurationEngine.setString(fileConfiguration, "arenaQuitHintMessage", "&4[EliteMobs] &cYou can leave the arena by doing &4/em quit");
        arenaJoinPlayerMessage = ConfigurationEngine.setString(fileConfiguration, "arenaJoinPlayerMessage", "&2[EliteMobs] &aYou can start the arena by doing &2/em start &aif there are at least &2$count &aplayers in it! \nYou can leave the arena by doing &c/em quit");
        arenaJoinSpectatorMessage = ConfigurationEngine.setString(fileConfiguration, "arenaJoinSpectatorMessage", "&2[EliteMobs] &aYou can leave the arena at any time using &2/em quit");
        waveTitle = ConfigurationEngine.setString(fileConfiguration, "waveTitle", "&aWave &2$wave");
        waveSubtitle = ConfigurationEngine.setString(fileConfiguration, "waveSubtitle", "");
        victoryTitle = ConfigurationEngine.setString(fileConfiguration, "victoryTitle", "&2Victory!");
        victorySubtitle = ConfigurationEngine.setString(fileConfiguration, "victorySubtitle", "&aCompleted &2$wave &awaves!");
        defeatTitle = ConfigurationEngine.setString(fileConfiguration, "defeatTitle", "&4Defeat!");
        defeatSubtitle = ConfigurationEngine.setString(fileConfiguration, "defeatSubtitle", "&cReached wave &4$wave&c!");
        victoryBroadcast = ConfigurationEngine.setString(fileConfiguration, "victoryBroadcast", "Arena $arenaName was conquered by $players!");
        joinPlayerTitle = ConfigurationEngine.setString(fileConfiguration, "joinPlayerTitle", "&aStart with &2/em start &a!");
        joinPlayerSubtitle = ConfigurationEngine.setString(fileConfiguration, "joinPlayerSubtitle", "&cLeave with &4/em quit &c!");
        joinSpectatorTitle = ConfigurationEngine.setString(fileConfiguration, "joinSpectatorTitle", "&aNow spectating!");
        joinSpectatorSubtitle = ConfigurationEngine.setString(fileConfiguration, "joinSpectatorSubtitle", "&cLeave with &4/em quit &c!");
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }
}
