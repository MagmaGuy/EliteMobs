package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class RelativeCoordinatesCommand {
    public static void get(Player player, String minidungeonString) {

        Minidungeon minidungeon = Minidungeon.minidungeons.get(minidungeonString);
        if (minidungeon == null) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Minidungeons name " + minidungeonString + " &4isn't valid!"));
            return;
        }

        if (!minidungeon.isInstalled) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Minidungeon isn't installed! Can't get the relative location for uninstalled Minidungeons!"));
            return;
        }

        Location anchorpoint = minidungeon.dungeonPackagerConfigFields.getAnchorPoint();

        if (anchorpoint == null) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Something went wrong and made the anchor point not valid!"));
            return;
        }

        String relativePosition = player.getLocation().clone().subtract(anchorpoint).getBlockX() + ", "
                + player.getLocation().clone().subtract(anchorpoint).getBlockY() + ", "
                + player.getLocation().clone().subtract(anchorpoint).getBlockZ();

        player.sendMessage(ChatColorConverter.convert(
                "[EliteMobs] Relative position to anchor point of " + minidungeon.dungeonPackagerConfigFields.getName() + ": " + relativePosition));

    }

}
