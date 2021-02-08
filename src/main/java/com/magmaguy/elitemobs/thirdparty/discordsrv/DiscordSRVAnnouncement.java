package com.magmaguy.elitemobs.thirdparty.discordsrv;

import com.magmaguy.elitemobs.config.DiscordSRVConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class DiscordSRVAnnouncement {

    private static TextChannel textChannel = null;
    private static boolean isInitialized = false;

    public DiscordSRVAnnouncement(String announcement) {

        if (Bukkit.getPluginManager().getPlugin("DiscordSRV") == null) return;
        if (DiscordSRVConfig.announcementRoomName.equals("YOU_NEED_TO_PUT_THE_NAME_OF_THE_DISCORD_ROOM_YOU_WANT_ELITEMOBS" +
                "_ANNOUNCEMENTS_TO_BE_BROADCASTED_IN_AS_YOU_HAVE_IN_YOUR_DISCORDSRV_CONFIGURATION_FILE_CHECK_ELITEMOBS_WIKI_FOR_DETAILS"))
            return;

        try {
            //Initialize which room will be used regardless of whether it's using config name, id or discord room name
            if (!isInitialized) {
                if (textChannel == null)
                    textChannel = DiscordUtil.getTextChannelById(DiscordSRVConfig.announcementRoomName);

                if (textChannel == null)
                    textChannel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(DiscordSRVConfig.announcementRoomName);

                if (textChannel == null)
                    if (DiscordUtil.getJda().getTextChannelsByName(DiscordSRVConfig.announcementRoomName, true).size() > 0)
                        textChannel = DiscordUtil.getJda().getTextChannelsByName(DiscordSRVConfig.announcementRoomName, true).get(0);

                isInitialized = true;
            }

            if (textChannel != null)
                textChannel.sendMessage(ChatColor.stripColor(announcement)).queue();
            else
                new WarningMessage("Channel room " + DiscordSRVConfig.announcementRoomName + " is not valid!");

        } catch (Exception ex) {
            new WarningMessage("Failed to send announcement via DiscordsSRV! Is it configured correctly?");
        }

    }


}
