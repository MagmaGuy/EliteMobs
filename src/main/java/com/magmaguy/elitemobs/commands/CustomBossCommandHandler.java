package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.dungeons.Minidungeon;
import com.magmaguy.elitemobs.mobconstructor.custombosses.AbstractRegionalEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.utils.DebugBlockLocation;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CustomBossCommandHandler {

    public static Location autoSeekSafeSpawnLocation(Location originalLocation) {
        Location newLocation = new Location(originalLocation.getWorld(),
                originalLocation.getBlockX() + 0.5,
                originalLocation.getBlockY() + 0.5,
                originalLocation.getBlockZ() + 0.5);
        for (int i = 0; i < 4; i++)
            if (newLocation.add(new Vector(0, i, 0)).getBlock().isPassable()) {
                new DebugBlockLocation(newLocation);
                return newLocation;
            }
        return null;
    }

    public static void addSpawnLocation(String customBossConfigFieldsString, Player player) {
        CustomBossConfigFields customBossConfigFields = CustomBossConfigFields.regionalElites.get(customBossConfigFieldsString);
        if (customBossConfigFields == null)
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Failed to add spawn location! Custom Boss " + customBossConfigFieldsString + " is not valid regional boss!"));
        else {
            Location safeSpawnLocation = autoSeekSafeSpawnLocation(player.getLocation());
            if (safeSpawnLocation == null)
                player.sendMessage("[EliteMobs] No safe spawn location found! Make sure the area is passable!");
            else
                new AbstractRegionalEntity(safeSpawnLocation, customBossConfigFields);
        }
    }

    public static void addRelativeSpawnLocation(Player player, String customBossConfigFieldsString, String minidungeonString) {
        Minidungeon minidungeon = Minidungeon.minidungeons.get(minidungeonString);
        if (minidungeon == null) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Failed to add relative location! Minidungeon is not valid!"));
            return;
        }
        CustomBossConfigFields customBossConfigFields = CustomBossConfigFields.regionalElites.get(customBossConfigFieldsString);
        if (customBossConfigFields == null)
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Failed to add relative location! Custom boss is not valid!"));
        else {
            Location safeSpawnLocation = autoSeekSafeSpawnLocation(player.getLocation());
            if (safeSpawnLocation == null)
                player.sendMessage("[EliteMobs] No safe spawn location found! Make sure the area is passable!");
            else
                minidungeon.initializeRelativeLocationAddition(customBossConfigFields, safeSpawnLocation);
        }
    }

    public static void setLeashRadius(String customBossConfigFieldsString, CommandSender commandSender, int leashRadius) {
        CustomBossConfigFields customBossConfigFields = CustomBossConfigFields.regionalElites.get(customBossConfigFieldsString);
        if (customBossConfigFields == null) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Failed set the leash radius! Was the boss a valid regional boss?"));
            return;
        }
        customBossConfigFields.setLeashRadius(leashRadius);
        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet())
            if (customBossConfigFields.getFileName().equals(regionalBossEntity.getCustomBossConfigFields().getFileName()))
                regionalBossEntity.setLeashRadius(leashRadius);
    }


}
