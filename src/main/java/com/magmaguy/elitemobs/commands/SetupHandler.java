package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.commands.setup.SetupMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SetupHandler {

    private String regionName = "";
    private Player player;

    public SetupHandler(Player player, String[] args) {

        if (args.length == 1) {
            new SetupMenu(player);
            return;
        }

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
        protectArea(player, regionName);
    }

    public static void protectArea(Player player, String regionName) {
        //elitemobs flags
        commandPackage("&3", "elitemobs-dungeon", "allow", player, regionName);
        commandPackage("&3", "elitemobs-antiexploit", "deny", player, regionName);
        commandPackage("&3", "elitemobs-events", "deny", player, regionName);

        //worldguard flags
        commandPackage("&a", "interact", "deny", player, regionName);
        commandPackage("&a", "creeper-explosion", "deny", player, regionName);
        commandPackage("&a", "fire-spread", "deny", player, regionName);
        commandPackage("&a", "lava-fire", "deny", player, regionName);
        commandPackage("&a", "lava-flow", "deny", player, regionName);
        commandPackage("&a", "snow-fall", "deny", player, regionName);
        commandPackage("&a", "snow-melt", "deny", player, regionName);
        commandPackage("&a", "ice-form", "deny", player, regionName);
        commandPackage("&a", "ice-melt", "deny", player, regionName);
        commandPackage("&a", "frosted-ice-melt", "deny", player, regionName);
        commandPackage("&a", "frosted-ice-form", "deny", player, regionName);
        commandPackage("&a", "leaf-decay", "false", player, regionName);
        commandPackage("&a", "grass-growth", "false", player, regionName);
        commandPackage("&a", "mycelium-spread", "deny", player, regionName);
        commandPackage("&a", "crop-growth", "deny", player, regionName);
        commandPackage("&a", "soil-dry", "deny", player, regionName);
        commandPackage("&a", "coral-fade", "deny", player, regionName);
        commandPackage("&a", "ravager-grief", "deny", player, regionName);
        commandPackage("&a", "ghast-fireball", "deny", player, regionName);
        commandPackage("&a", "wither-damage", "deny", player, regionName);
        commandPackage("&a", "enderman-grief", "deny", player, regionName);
        commandPackage("&a", "item-frame-rotation", "deny", player, regionName);
        commandPackage("&a", "vehicle-place", "deny", player, regionName);
        commandPackage("&a", "vehicle-destroy", "deny", player, regionName);
        commandPackage("&a", "pvp", "deny", player, regionName);
        commandPackage("&a", "other-explosion", "deny", player, regionName);
        commandPackage("&a", "block-trampling", "deny", player, regionName);
        commandPackage("&a", "vine-growth", "deny", player, regionName);
        commandPackage("&a", "mushroom-growth", "deny", player, regionName);
        commandPackage("&a", "damage-animals", "allow", player, regionName);
        commandPackage("&a", "sleep", "deny", player, regionName);
        commandPackage("&a", "chest-access", "allow", player, regionName);
        commandPackage("&a", "entity-painting-destroy", "deny", player, regionName);
        commandPackage("&a", "mob-spawning", "allow", player, regionName);
        commandPackage("&a", "tnt", "deny", player, regionName);
        commandPackage("&a", "ender-dragon-block-damage", "deny", player, regionName);
        commandPackage("&a", "lighter", "deny", player, regionName);
        commandPackage("&a", "enderpearl", "deny", player, regionName);

        //worldguardextraflags
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuardExtraFlags")) {
            commandPackage("&2", "fly", "deny", player, regionName);
        } else {
            player.sendMessage(ChatColor.RED + "[EliteMobs] Warning: the WorldGuardExtraFlags plugin is not present. It is recommended for the use of the anti-flight flag.");
        }

        player.performCommand("rg info");

    }

    private static void commandPackage(String colorCode, String flagString, String state, Player player, String regionName) {
        player.sendMessage(flagString(colorCode + flagString, player));
        player.performCommand(commandString(flagString, state, player, regionName));
    }

    private static String flagString(String flagString, Player player) {
        return ChatColorConverter.convert("&a[EliteMobs] Adding flag " + flagString);
    }

    private static String commandString(String flagString, String state, Player player, String regionName) {
        return "region flag " + regionName + " " + flagString + " " + state;
    }

}
