package com.magmaguy.elitemobs.thirdparty.discordsrv;

import com.magmaguy.elitemobs.config.DiscordSRVConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class DiscordSRVAnnouncement {

    public DiscordSRVAnnouncement(String announcement) {
        if (!Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) return;
        if (DiscordSRVConfig.announcementRoomName.equals("YOU_NEED_TO_PUT_THE_NAME_OF_THE_DISCORD_ROOM_YOU_WANT_ELITEMOBS" +
                "_ANNOUNCEMENTS_TO_BE_BROADCASTED_IN_AS_YOU_HAVE_IN_YOUR_DISCORDSRV_CONFIGURATION_FILE")) return;
        if (DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(DiscordSRVConfig.announcementRoomName) != null)
            DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(DiscordSRVConfig.announcementRoomName).sendMessage(ChatColor.stripColor(announcement)).queue();
        else
            new WarningMessage("Channel room " + DiscordSRVConfig.announcementRoomName + " is not valid!");
    }

}
