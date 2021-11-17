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
            if (minidungeon.getDungeonPackagerConfigFields().getDungeonLocationType().equals(DungeonPackagerConfigFields.DungeonLocationType.SCHEMATIC))
                PlayerPreTeleportEvent.teleportPlayer(player, minidungeon.getTeleportLocation());
            else
                PlayerPreTeleportEvent.teleportPlayer(player,
                        new Location(minidungeon.getTeleportLocation().getWorld(),
                                minidungeon.getTeleportLocation().getX(),
                                minidungeon.getTeleportLocation().getY(),
                                minidungeon.getTeleportLocation().getZ(),
                                Float.parseFloat("" + minidungeon.getDungeonPackagerConfigFields().getTeleportPointPitch()),
                                Float.parseFloat("" + minidungeon.getDungeonPackagerConfigFields().getTeleportPointYaw())));
        else
            player.sendMessage("[EliteMobs] That dungeon isn't valid!");
    }
}
