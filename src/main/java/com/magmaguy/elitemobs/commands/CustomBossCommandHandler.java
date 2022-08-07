package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.dungeons.SchematicDungeonPackage;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.utils.DebugBlockLocation;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CustomBossCommandHandler {
    private CustomBossCommandHandler() {
    }

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
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfigFields.getRegionalElites().get(customBossConfigFieldsString);
        if (customBossesConfigFields == null)
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Failed to add spawn location! Custom Boss " + customBossConfigFieldsString + " is not valid regional boss!"));
        else {
            Location safeSpawnLocation = autoSeekSafeSpawnLocation(player.getLocation());
            if (safeSpawnLocation == null)
                player.sendMessage("[EliteMobs] No safe spawn location found! Make sure the area is passable!");
            else {
                RegionalBossEntity.createPermanentRegionalBossEntity(customBossesConfigFields, safeSpawnLocation);
            }
        }
    }

    public static void addRelativeSpawnLocation(Player player, String customBossConfigFieldsString, String minidungeonString) {
        EMPackage emPackage = EMPackage.getEmPackages().get(minidungeonString);
        if (emPackage == null) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Failed to add relative location! Minidungeon is not valid!"));
            return;
        }
        if (!(emPackage instanceof SchematicDungeonPackage)) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Target EM package was not a schematic dungeon so this command won't work!"));
            return;
        }
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfigFields.getRegionalElites().get(customBossConfigFieldsString);
        if (customBossesConfigFields == null)
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Failed to add relative location! Custom boss is not valid!"));
        else {
            Location safeSpawnLocation = autoSeekSafeSpawnLocation(player.getLocation());
            if (safeSpawnLocation == null)
                player.sendMessage("[EliteMobs] No safe spawn location found! Make sure the area is passable!");
            else
                ((SchematicDungeonPackage) emPackage).addBoss(customBossesConfigFields, safeSpawnLocation);
        }
    }

    public static void setLeashRadius(String customBossConfigFieldsString, CommandSender commandSender, int leashRadius) {
        CustomBossesConfigFields customBossesConfigFields = CustomBossesConfigFields.getRegionalElites().get(customBossConfigFieldsString);
        if (customBossesConfigFields == null) {
            commandSender.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Failed set the leash radius! Was the boss a valid regional boss?"));
            return;
        }
        customBossesConfigFields.runtimeSetLeashRadius(leashRadius);
        for (RegionalBossEntity regionalBossEntity : RegionalBossEntity.getRegionalBossEntitySet())
            if (customBossesConfigFields.getFilename().equals(regionalBossEntity.getCustomBossesConfigFields().getFilename()))
                regionalBossEntity.setLeashRadius(leashRadius);
    }


}
