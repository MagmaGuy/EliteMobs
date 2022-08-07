package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import org.bukkit.entity.Player;

public class DungeonCommands {
    public static void teleport(Player player, String minidungeonName) {
        EMPackage emPackage = EMPackage.getEmPackages().get(minidungeonName);
        if (emPackage != null)
            PlayerPreTeleportEvent.teleportPlayer(player, emPackage.getDungeonPackagerConfigFields().getTeleportLocation());
        else
            player.sendMessage("[EliteMobs] That dungeon isn't valid!");
    }
}
