package com.magmaguy.elitemobs.thirdparty.discordsrv;

import com.magmaguy.elitemobs.config.DiscordSRVConfig;
import com.magmaguy.magmacore.util.Logger;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class DiscordSRVAnnouncement {

    public DiscordSRVAnnouncement(String announcement) {

        if (Bukkit.getPluginManager().getPlugin("DiscordSRV") == null) return;
        if (DiscordSRVConfig.getAnnouncementRoomName().equals("YOU_NEED_TO_PUT_THE_NAME_OF_THE_DISCORD_ROOM_YOU_WANT_ELITEMOBS" +
                "_ANNOUNCEMENTS_TO_BE_BROADCASTED_IN_AS_YOU_HAVE_IN_YOUR_DISCORDSRV_CONFIGURATION_FILE_CHECK_ELITEMOBS_WIKI_FOR_DETAILS"))
            return;

        try {
            // These are JDA's in-memory lookups. Resolve against current configuration and
            // provider state for each announcement rather than retaining a stale destination.
            String room = DiscordSRVConfig.getAnnouncementRoomName();
            TextChannel channel = room.matches("[0-9]+") ? DiscordUtil.getTextChannelById(room) : null;
            if (channel == null) channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(room);
            if (channel == null) {
                var matches = DiscordUtil.getJda().getTextChannelsByName(room, true);
                if (!matches.isEmpty()) channel = matches.get(0);
            }
            if (channel == null) {
                Logger.warn("Channel room " + room + " is not valid!");
                return;
            }
            channel.sendMessage(ChatColor.stripColor(announcement)).queue(ignored -> {},
                    failure -> Logger.warn("DiscordSRV announcement failed for " + room + ": " + failure.getMessage()));

        } catch (Exception ex) {
            Logger.warn("Failed to send announcement via DiscordsSRV! Is it configured correctly?");
        }

    }


}
