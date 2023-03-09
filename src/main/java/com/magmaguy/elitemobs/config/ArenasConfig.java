package com.magmaguy.elitemobs.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.List;

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
    @Getter
    private static String noArenaPermissionMessage;

    private ArenasConfig() {
    }

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("Arenas.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        notEnoughPlayersMessage = ConfigurationEngine.setString(
                List.of("Message that appears when not enough players are present to start an arena."),
                file, fileConfiguration, "notEnoughPlayersMessage", "&8[EliteMobs] &cYou need at least $amount to start the match!", true);
        startingTitle = ConfigurationEngine.setString(
                List.of("Title that appears when an arena is starting."),
                file, fileConfiguration, "startingMessage", "&2Starting!", true);
        startingSubtitle = ConfigurationEngine.setString(
                List.of("Subtitle that appears when an arena is starting."),
                file, fileConfiguration, "startingSubtitle", "&2in $count...", true);
        arenaFullMessage = ConfigurationEngine.setString(
                List.of("Message that appears when an arena is full."),
                file, fileConfiguration, "arenaFullMessage", "&4[EliteMobs] &cArena is full! You can spectate instead while you wait for it to finish!", true);
        arenasOngoingMessage = ConfigurationEngine.setString(
                List.of("Message that appears when a player attempts to join an active arena."),
                file, fileConfiguration, "arenasOngoingMessage", "&4[EliteMobs] &cCan't join the arena now - a match is currently happening! You can spectate instead while you wait for it to finish!", true);
        arenaStartHintMessage = ConfigurationEngine.setString(
                List.of("Message that appears to remind players how to start an arena after joining as a player."),
                file, fileConfiguration, "instanceStartHintMessage", "&2[EliteMobs] &aYou can start the instance by doing &2/em start", true);
        arenaQuitHintMessage = ConfigurationEngine.setString(
                List.of("Message that appears to remind players how to leave an arena."),
                file, fileConfiguration, "instanceQuitHintMessage", "&4[EliteMobs] &cYou can leave the instance by doing &4/em quit", true);
        arenaJoinPlayerMessage = ConfigurationEngine.setString(
                List.of("Message that appears when players join an arena."),
                file, fileConfiguration, "arenaJoinPlayerMessage", "&2[EliteMobs] &aYou can start the arena by doing &2/em start &aif there are at least &2$count &aplayers in it! \nYou can leave the arena by doing &c/em quit", true);
        arenaJoinSpectatorMessage = ConfigurationEngine.setString(
                List.of("Message that appears to remind players how to leave an arena after joining as a spectator."),
                file, fileConfiguration, "arenaJoinSpectatorMessage", "&2[EliteMobs] &aYou can leave the arena at any time using &2/em quit", true);
        waveTitle = ConfigurationEngine.setString(
                List.of("Title message that appears when a wave is starting."),
                file, fileConfiguration, "waveTitle", "&aWave &2$wave", true);
        waveSubtitle = ConfigurationEngine.setString(
                List.of("Subtitle message that appears when a wave is starting."),
                file, fileConfiguration, "waveSubtitle", "", true);
        victoryTitle = ConfigurationEngine.setString(
                List.of("Title that appears when an arena is completed."),
                file, fileConfiguration, "victoryTitle", "&2Victory!", true);
        victorySubtitle = ConfigurationEngine.setString(
                List.of("Subtitle that appears when an arena is completed."),
                file, fileConfiguration, "victorySubtitle", "&aCompleted &2$wave &awaves!", true);
        defeatTitle = ConfigurationEngine.setString(
                List.of("Title that appears when players fail an arena."),
                file, fileConfiguration, "defeatTitle", "&4Defeat!", true);
        defeatSubtitle = ConfigurationEngine.setString(
                List.of("Subitle that appears when players fail an arena."),
                file, fileConfiguration, "defeatSubtitle", "&cReached wave &4$wave&c!", true);
        victoryBroadcast = ConfigurationEngine.setString(
                List.of("Message broadcasted when players beat an arena."),
                file, fileConfiguration, "victoryBroadcast", "Arena $arenaName was conquered by $players!", true);
        joinPlayerTitle = ConfigurationEngine.setString(
                List.of("Title that appears when a player joins an arena."),
                file, fileConfiguration, "joinPlayerTitle", "&aStart with &2/em start &a!", true);
        joinPlayerSubtitle = ConfigurationEngine.setString(
                List.of("Subtitle that appears when a player joins an arena."),
                file, fileConfiguration, "joinPlayerSubtitle", "&cLeave with &4/em quit &c!", true);
        joinSpectatorTitle = ConfigurationEngine.setString(
                List.of("Title that appears when a spectator joins an arena."),
                file, fileConfiguration, "joinSpectatorTitle", "&aNow spectating!", true);
        joinSpectatorSubtitle = ConfigurationEngine.setString(
                List.of("Subtitle that appears when a spectator joins an arena."),
                file, fileConfiguration, "joinSpectatorSubtitle", "&cLeave with &4/em quit &c!", true);
        noArenaPermissionMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players if they do not have permission to enter an arena."),
                file, fileConfiguration, "noArenaPermissionMessage", "[EliteMobs] You don't have the permission to enter this arena!", true);
        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }
}
