package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class StringColorAnimator {

    public static void startTitleAnimation(Player player, String title, String subtitle, ChatColor primaryColor, ChatColor secondaryColor) {

        new BukkitRunnable() {
            int counter = 0;
            int titleIndex = 0;
            int subtitleIndex = 0;
            int titleSize = title.length();
            int subtitleSize = subtitle.length();

            @Override
            public void run() {

                counter++;

                if (titleIndex <= titleSize) {
                    StringBuilder newTitle = new StringBuilder(title).insert(titleIndex, primaryColor);
                    if (titleIndex > 1)
                        newTitle.insert(titleIndex - 2, secondaryColor);
                    titleIndex++;
                    player.sendTitle(primaryColor + ChatColorConverter.convert(newTitle.toString()),
                            secondaryColor + ChatColorConverter.convert(subtitle), 0, 5, 0);
                    return;
                }

                if (subtitleIndex > subtitleSize) {
                    cancel();
                    return;
                }

                StringBuilder newSubtitle = new StringBuilder(subtitle).insert(subtitleIndex, secondaryColor);
                if (subtitleIndex > 1)
                    newSubtitle.insert(subtitleIndex - 2, primaryColor);
                subtitleIndex++;
                player.sendTitle("",
                        secondaryColor + ChatColorConverter.convert(newSubtitle.toString()), 0, 5, 0);

            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 2);

    }

}
