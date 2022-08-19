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

        notEnoughPlayersMessage = ConfigurationEngine.setString(file, fileConfiguration, "notEnoughPlayersMessage", "&8[EliteMobs] &cYou need at least $amount to start the match!", true);
        startingTitle = ConfigurationEngine.setString(file, fileConfiguration, "startingMessage", "&2Starting!", true);
        startingSubtitle = ConfigurationEngine.setString(file, fileConfiguration, "startingSubtitle", "&2in $count...", true);
        arenaFullMessage = ConfigurationEngine.setString(file, fileConfiguration, "arenaFullMessage", "&4[EliteMobs] &cArena is full! You can spectate instead while you wait for it to finish!", true);
        arenasOngoingMessage = ConfigurationEngine.setString(file, fileConfiguration, "arenasOngoingMessage", "&4[EliteMobs] &cCan't join the arena now - a match is currently happening! You can spectate instead while you wait for it to finish!", true);
        arenaStartHintMessage = ConfigurationEngine.setString(file, fileConfiguration, "arenaStartHintMessage", "&2[EliteMobs] &aYou can start the arena by doing &2/em start", true);
        arenaQuitHintMessage = ConfigurationEngine.setString(file, fileConfiguration, "arenaQuitHintMessage", "&4[EliteMobs] &cYou can leave the arena by doing &4/em quit", true);
        arenaJoinPlayerMessage = ConfigurationEngine.setString(file, fileConfiguration, "arenaJoinPlayerMessage", "&2[EliteMobs] &aYou can start the arena by doing &2/em start &aif there are at least &2$count &aplayers in it! \nYou can leave the arena by doing &c/em quit", true);
        arenaJoinSpectatorMessage = ConfigurationEngine.setString(file, fileConfiguration, "arenaJoinSpectatorMessage", "&2[EliteMobs] &aYou can leave the arena at any time using &2/em quit", true);
        waveTitle = ConfigurationEngine.setString(file, fileConfiguration, "waveTitle", "&aWave &2$wave", true);
        waveSubtitle = ConfigurationEngine.setString(file, fileConfiguration, "waveSubtitle", "", true);
        victoryTitle = ConfigurationEngine.setString(file, fileConfiguration, "victoryTitle", "&2Victory!", true);
        victorySubtitle = ConfigurationEngine.setString(file, fileConfiguration, "victorySubtitle", "&aCompleted &2$wave &awaves!", true);
        defeatTitle = ConfigurationEngine.setString(file, fileConfiguration, "defeatTitle", "&4Defeat!", true);
        defeatSubtitle = ConfigurationEngine.setString(file, fileConfiguration, "defeatSubtitle", "&cReached wave &4$wave&c!", true);
        victoryBroadcast = ConfigurationEngine.setString(file, fileConfiguration, "victoryBroadcast", "Arena $arenaName was conquered by $players!", true);
        joinPlayerTitle = ConfigurationEngine.setString(file, fileConfiguration, "joinPlayerTitle", "&aStart with &2/em start &a!", true);
        joinPlayerSubtitle = ConfigurationEngine.setString(file, fileConfiguration, "joinPlayerSubtitle", "&cLeave with &4/em quit &c!", true);
        joinSpectatorTitle = ConfigurationEngine.setString(file, fileConfiguration, "joinSpectatorTitle", "&aNow spectating!", true);
        joinSpectatorSubtitle = ConfigurationEngine.setString(file, fileConfiguration, "joinSpectatorSubtitle", "&cLeave with &4/em quit &c!", true);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }
}
