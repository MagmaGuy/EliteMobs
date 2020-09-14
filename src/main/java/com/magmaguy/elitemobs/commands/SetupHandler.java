package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EliteMobs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SetupHandler {

    private String regionName = "";
    private Player player;

    public SetupHandler(Player player, String[] args) {
        if (!EliteMobs.worldguardIsEnabled) {
            player.sendMessage("[EliteMobs] You don't have WorldGuard installed! It is not possible to correctly set " +
                    "up a lair/minidungeon/dungeon without that plugin!");
            return;
        }

        //Syntax: /em setup area [regionName]

        if (args.length < 3) {
            player.sendMessage("[EliteMobs] Invalid command syntax! Expected syntax:");
            player.sendMessage("/em setup area [regionName]");
            player.sendMessage("[regionName] is the name of your WorldGuard protected cuboid! Create the region first!");
            return;
        }

        regionName = args[2];
        this.player = player;

        //elitemobs flags
        commandPackage("&3", "elitemobs-dungeon", "allow");
        commandPackage("&3", "elitemobs-antiexploit", "deny");
        commandPackage("&3", "elitemobs-events", "deny");

        //worldguard flags
        commandPackage("&a", "interact", "deny");
        commandPackage("&a", "creeper-explosion", "deny");
        commandPackage("&a", "fire-spread", "deny");
        commandPackage("&a", "lava-fire", "deny");
        commandPackage("&a", "snow-fall", "deny");
        commandPackage("&a", "snow-melt", "deny");
        commandPackage("&a", "ice-form", "deny");
        commandPackage("&a", "ice-melt", "deny");
        commandPackage("&a", "frosted-ice-melt", "deny");
        commandPackage("&a", "frosted-ice-form", "deny");
        commandPackage("&a", "leaf-decay", "false");
        commandPackage("&a", "grass-growth", "false");
        commandPackage("&a", "mycelium-spread", "deny");
        commandPackage("&a", "crop-growth", "deny");
        commandPackage("&a", "soil-dry", "deny");
        commandPackage("&a", "coral-fade", "deny");
        commandPackage("&a", "ravager-grief", "deny");
        commandPackage("&a", "ghast-fireball", "deny");
        commandPackage("&a", "wither-damage", "deny");
        commandPackage("&a", "enderman-grief", "deny");
        commandPackage("&a", "item-frame-rotation", "deny");
        commandPackage("&a", "vehicle-place", "deny");
        commandPackage("&a", "vehicle-destroy", "deny");
        commandPackage("&a", "pvp", "deny");
        commandPackage("&a", "other-explosion", "deny");
        commandPackage("&a", "block-trampling", "deny");
        commandPackage("&a", "vine-growth", "deny");
        commandPackage("&a", "mushroom-growth", "deny");
        commandPackage("&a", "damage-animals", "allow");
        commandPackage("&a", "sleep", "deny");
        commandPackage("&a", "chest-access", "allow");
        commandPackage("&a", "entity-painting-destroy", "deny");
        commandPackage("&a", "mob-spawning", "allow");
        commandPackage("&a", "tnt", "deny");
        commandPackage("&a", "ender-dragon-block-damage", "deny");
        commandPackage("&a", "lighter", "deny");
        commandPackage("&a", "enderpearl", "deny");

        //worldguardextraflags
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuardExtraFlags")) {
            commandPackage("&2", "fly", "deny");
        } else {
            player.sendMessage(ChatColor.RED + "[EliteMobs] Warning: the WorldGuardExtraFlags plugin is not present. It is recommended for the use of the anti-flight flag.");
        }

        player.performCommand("rg info");

    }

    private void commandPackage(String colorCode, String flagString, String state) {
        player.sendMessage(flagString(colorCode + flagString));
        player.performCommand(commandString(flagString, state));
    }

    private String flagString(String flagString) {
        return ChatColorConverter.convert("&a[EliteMobs] Adding flag " + flagString);
    }

    private String commandString(String flagString, String state) {
        return "region flag " + regionName + " " + flagString + " " + state;
    }

}
