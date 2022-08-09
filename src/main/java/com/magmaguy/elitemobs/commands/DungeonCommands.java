package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import org.bukkit.entity.Player;

public class DungeonCommands {
    public static void teleport(Player player, String minidungeonName) {
        EMPackage emPackage = EMPackage.getEmPackages().get(minidungeonName);
        if (emPackage != null) {
            if (emPackage.getDungeonPackagerConfigFields().getTeleportLocation() != null) {
                PlayerPreTeleportEvent.teleportPlayer(player, emPackage.getDungeonPackagerConfigFields().getTeleportLocation());
            } else {
                player.sendMessage("[EliteMobs] Can't teleport you to the dungeon because the teleport location isn't set! Ask an admin to reinstall the dungeon!");
            }
        } else
            player.sendMessage("[EliteMobs] That dungeon isn't valid!");
    }
}
