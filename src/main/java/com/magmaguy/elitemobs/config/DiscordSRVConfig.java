package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

public class DiscordSRVConfig extends ConfigurationFile {

    @Getter
    private static String announcementRoomName;

    public DiscordSRVConfig() {
        super("DiscordSRV.yml");
    }

    @Override
    public void initializeValues() {
        announcementRoomName = ConfigurationEngine.setString(
                List.of("Documentation can be found here: https://github.com/MagmaGuy/EliteMobs/wiki/DiscordSRV---Discord-broadcasts"),
                file, fileConfiguration, "announcementRoomName",
                "YOU_NEED_TO_PUT_THE_NAME_OF_THE_DISCORD_ROOM_YOU_WANT_ELITEMOBS" +
                        "_ANNOUNCEMENTS_TO_BE_BROADCASTED_IN_AS_YOU_HAVE_IN_YOUR_DISCORDSRV_" +
                        "CONFIGURATION_FILE_CHECK_ELITEMOBS_WIKI_FOR_DETAILS", false);

    }
}
