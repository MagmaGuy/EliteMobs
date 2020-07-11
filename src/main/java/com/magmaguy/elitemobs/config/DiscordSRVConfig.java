package com.magmaguy.elitemobs.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class DiscordSRVConfig {

    public static String announcementRoomName;

    public static void initializeConfig() {
        File file = ConfigurationEngine.fileCreator("DiscordSRV.yml");
        FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);

        announcementRoomName = ConfigurationEngine.setString(fileConfiguration, "announcementRoomName",
                "YOU_NEED_TO_PUT_THE_NAME_OF_THE_DISCORD_ROOM_YOU_WANT_ELITEMOBS" +
                        "_ANNOUNCEMENTS_TO_BE_BROADCASTED_IN_AS_YOU_HAVE_IN_YOUR_DISCORDSRV_" +
                        "CONFIGURATION_FILE_CHECK_ELITEMOBS_WIKI_FOR_DETAILS");

        ConfigurationEngine.fileSaverOnlyDefaults(fileConfiguration, file);
    }

}
