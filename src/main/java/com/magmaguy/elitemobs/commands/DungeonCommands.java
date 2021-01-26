package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DungeonCommands {
    public static void teleport(Player player, String minidungeonName) {
        Minidungeon minidungeon = Minidungeon.minidungeons.get(minidungeonName);
        if (minidungeon != null)
            if (minidungeon.dungeonPackagerConfigFields.getDungeonLocationType().equals(DungeonPackagerConfigFields.DungeonLocationType.SCHEMATIC))
                PlayerPreTeleportEvent.teleportPlayer(player, minidungeon.teleportLocation);
            else
                PlayerPreTeleportEvent.teleportPlayer(player,
                        new Location(minidungeon.teleportLocation.getWorld(),
                                minidungeon.teleportLocation.getX(),
                                minidungeon.teleportLocation.getY(),
                                minidungeon.teleportLocation.getZ(),
                                Float.parseFloat("" + minidungeon.dungeonPackagerConfigFields.getTeleportPointPitch()),
                                Float.parseFloat("" + minidungeon.dungeonPackagerConfigFields.getTeleportPointYaw())));
        else
            player.sendMessage("[EliteMobs] That dungeon isn't valid!");
    }
}
